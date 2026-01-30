#   Copyright (C) 2025-2026 Credit Mutuel Arkea
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
"""
Run or resume a RAG experiment on a Langfuse dataset.

Usage:
    run_experiment.py [-v] --json-config-file=<jcf>

Description:
    • If *dataset_experiment.experiment_name* in the JSON config **already ends with a UUID
      (the pattern “-XXXXXXXX”)**, the script resumes that existing experiment and
      skips items that are already present in the Langfuse run.

    • If it **does not** contain a UUID, a fresh experiment is started: the script
      appends “-XXXXXXXX” (first 8 chars of a new UUID-4) to the name exactly like
      the previous version.

Arguments:
    --json-config-file=<jcf>   Path to the input config file. This is a required argument.
Options:
    -v                         Enable verbose output for debugging purposes. If not set, the script runs silently except for errors.
    -h, --help                 Display this help message and exit.
    --version                  Display the version of the script.

Examples:
    python run_experiment.py --json-config-file=path/to/config-file.json
"""
import asyncio
import json
import os
import re
from datetime import datetime
from typing import List, Optional
from uuid import uuid4

from docopt import docopt
from langfuse import Langfuse
from langfuse._client.datasets import DatasetItemClient
from langfuse.api import NotFoundError
from langfuse.langchain import CallbackHandler as LangfuseCallbackHandler
from scripts.common.logging_config import configure_logging
from scripts.common.models import ActivityStatus, StatusWithReason
from scripts.dataset.evaluation.models import DatasetExperiment
from scripts.dataset.experiment.models import (
    RunExperimentInput,
    RunExperimentOutput,
)

from gen_ai_orchestrator.routers.requests.requests import RAGRequest
from gen_ai_orchestrator.services.langchain.rag_chain import execute_rag_chain
from gen_ai_orchestrator.services.security.security_service import (
    fetch_secret_key_value,
)

UUID_SUFFIX_RE = re.compile(r'.*-[0-9a-fA-F]{8}$')


def _deep_copy_without_keys(data: dict, exclude_paths: list[str]) -> dict:
    import copy

    exclude = set(exclude_paths)
    result = copy.deepcopy(data)

    def recurse(obj, parents=()):
        if isinstance(obj, dict):
            for k in list(obj):
                joined = '.'.join((*parents, k))
                if k in exclude or joined in exclude:
                    obj.pop(k, None)
                    continue
                recurse(obj[k], (*parents, k))
        elif isinstance(obj, list):
            for item in obj:
                recurse(item, parents)

    recurse(result)
    return result


def _llm_setting_to_web(llm) -> dict | None:
    if llm is None:
        return None

    d = {
        'apiKey': str(llm.api_key.secret),
        'apiVersion': str(llm.api_version),
        'deploymentName': str(llm.deployment_name),
        'model': str(llm.model),
        'apiBase': str(llm.api_base),
        'provider': str(llm.provider.value),
    }
    temp = getattr(llm, 'temperature', None)
    if temp is not None:
        d['temperature'] = str(temp)
    return d


def _build_web_rag_settings(rq: RAGRequest) -> dict:
    qa_prompt = rq.question_answering_prompt

    web: dict = {
        'noAnswerSentence': qa_prompt.inputs.get('no_answer'),
        'indexSessionId': 'TO_BE_UPDATED',
        'maxDocumentsRetrieved': rq.document_search_params.k,
        'questionAnsweringPrompt': {
            'formatter': qa_prompt.formatter,
            'template': qa_prompt.template,
        },
        'questionAnsweringLlmSetting': _llm_setting_to_web(
            rq.question_answering_llm_setting
        ),
        'emSetting': _llm_setting_to_web(rq.embedding_question_em_setting),
    }

    return {k: v for k, v in web.items() if v is not None}


def _build_web_compressor_settings(cs) -> dict:
    if cs is None:
        return {}
    return {
        'setting': {
            'label': cs.label,
            'endpoint': cs.endpoint,
            'minScore': cs.min_score,
            'maxDocuments': cs.max_documents,
            'provider': cs.provider,
        }
    }


async def main():
    start_time = datetime.now()
    cli_args = docopt(__doc__, version='Run Experiment 1.0.0')
    logger = configure_logging(cli_args)

    dataset_experiment = DatasetExperiment()
    dataset_items: List[DatasetItemClient] = []
    tested_items: List[DatasetItemClient] = []
    rag_query: Optional[RAGRequest] = None

    try:
        logger.info('Loading input data...')
        input_config = RunExperimentInput.from_json_file(cli_args['--json-config-file'])
        location = f"{input_config.bot.file_location}/{input_config.bot.namespace}-{input_config.bot.bot_id}/output"
        os.makedirs(location, exist_ok=True)
        dataset_experiment = input_config.dataset_experiment

        base_name = dataset_experiment.experiment_name
        resume_run = bool(UUID_SUFFIX_RE.fullmatch(base_name))

        if resume_run:
            experiment_name = base_name
            logger.info(
                "Detected UUID in experiment name – will resume '%s'.", experiment_name
            )
        else:
            experiment_name = f"{base_name}-{str(uuid4())[:8]}"
            logger.info("Starting new experiment '%s'.", experiment_name)

        dataset_experiment.experiment_name = experiment_name
        rag_query = input_config.rag_query

        client = Langfuse(
            host=str(rag_query.observability_setting.url),
            public_key=rag_query.observability_setting.public_key,
            secret_key=fetch_secret_key_value(
                rag_query.observability_setting.secret_key
            ),
        )

        dataset = client.get_dataset(dataset_experiment.dataset_name)
        dataset_items = dataset.items

        processed_item_ids = set()
        if resume_run:
            try:
                dataset_run = client.api.datasets.get_run(
                    dataset_name=dataset_experiment.dataset_name,
                    run_name=experiment_name,
                )
                processed_item_ids = {
                    ri.dataset_item_id for ri in dataset_run.dataset_run_items
                }
                logger.info(
                    'Found existing run with %d processed items (will skip them).',
                    len(processed_item_ids),
                )
            except NotFoundError:
                logger.warning(
                    "No existing run named '%s' found – starting a fresh run.",
                    experiment_name,
                )
                processed_item_ids.clear()

        for item in dataset_items:
            if item.id in processed_item_ids:
                logger.debug('Skipping item %s (already processed).', item.id)
                continue

            with item.run(
                run_name=experiment_name,
                run_description=dataset_experiment.experiment_description,
                run_metadata={
                    'llm': {
                        'provider': rag_query.question_answering_llm_setting.provider,
                        'model': rag_query.question_answering_llm_setting.model,
                        'temperature': rag_query.question_answering_llm_setting.temperature,
                    },
                    'embedding': {
                        'provider': rag_query.embedding_question_em_setting.provider,
                        'model': rag_query.embedding_question_em_setting.model,
                    },
                    'vector_store': rag_query.vector_store_setting.provider,
                    'document_index_name': rag_query.document_index_name,
                    'k': rag_query.document_search_params.k,
                },
            ) as root_span:
                observability_handler = LangfuseCallbackHandler(update_trace=True)
                rag_query.question_answering_prompt.inputs['question'] = item.input[
                    'question'
                ]

                await execute_rag_chain(
                    request=rag_query,
                    debug=False,
                    custom_observability_handler=observability_handler,
                )
                tested_items.append(item)

                print(
                    f"Waiting for rate limit delay ({input_config.rate_limit_delay}s)..."
                )
                await asyncio.sleep(input_config.rate_limit_delay)

        export_cfg = input_config.export_settings or {}

        # RagSettings
        rag_dict = _build_web_rag_settings(rag_query)
        rag_dict = _deep_copy_without_keys(
            rag_dict, export_cfg.get('rag_exclude_keys', [])
        )
        rag_file = f"{location}/{dataset_experiment.experiment_name}-RagSettings.json"
        with open(rag_file, 'w', encoding='utf-8') as f:
            json.dump(rag_dict, f, indent=2, ensure_ascii=False)

        # CompressorSettings
        comp_dict = _build_web_compressor_settings(rag_query.compressor_setting)
        comp_dict = _deep_copy_without_keys(
            comp_dict, export_cfg.get('compressor_exclude_keys', [])
        )
        comp_file = (
            f"{location}/{dataset_experiment.experiment_name}-CompressorSettings.json"
        )
        with open(comp_file, 'w', encoding='utf-8') as f:
            json.dump(comp_dict, f, indent=2, ensure_ascii=False)

        client.flush()
        activity_status = StatusWithReason(status=ActivityStatus.COMPLETED)

    except Exception as e:
        full_exception_name = f"{type(e).__module__}.{type(e).__name__}"
        activity_status = StatusWithReason(
            status=ActivityStatus.FAILED, status_reason=f"{full_exception_name} : {e}"
        )
        logger.error(e, exc_info=True)

    len_dataset_items = len(dataset_items)
    output = RunExperimentOutput(
        status=activity_status,
        rag_query=rag_query,
        dataset_experiment=dataset_experiment,
        duration=datetime.now() - start_time,
        items_count=len(dataset_items),
        success_rate=100 * (len(tested_items) / len_dataset_items)
        if len_dataset_items > 0
        else 0,
    )
    logger.debug(f"\n{output.format()}")


if __name__ == '__main__':
    asyncio.run(main())
