"""
Run RAG experiment on Langfuse dataset

Usage:
    run_experiment.py [-v] --json-config-file=<jcf>

Description:
    This script is used to run an experiment on Langfuse dataset based on a json configuration file.
    The configuration file specifies the RAG settings to use.
    It builds a RAG chain from the RAG Query and runs it against the provided LangFuse dataset.


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
import time
from datetime import datetime
from typing import List, Optional
from uuid import uuid4

from docopt import docopt
from gen_ai_orchestrator.routers.requests.requests import RagQuery
from gen_ai_orchestrator.services.langchain.rag_chain import create_rag_chain, execute_rag_chain
from gen_ai_orchestrator.services.security.security_service import fetch_secret_key_value
from langfuse import Langfuse
from langfuse.client import DatasetItemClient

from scripts.common.logging_config import configure_logging
from scripts.common.models import StatusWithReason, ActivityStatus
from scripts.dataset.evaluation.models import DatasetExperiment
from scripts.dataset.experiment.models import RunExperimentInput, RunExperimentOutput

async def main():
    start_time = datetime.now()
    cli_args = docopt(__doc__, version='Run Experiment 1.0.0')
    logger = configure_logging(cli_args)

    dataset_experiment = DatasetExperiment()
    dataset_items: List[DatasetItemClient] = []
    tested_items: List[DatasetItemClient] = []
    rag_query: Optional[RagQuery] = None
    try:
        logger.info("Loading input data...")
        input_config = RunExperimentInput.from_json_file(cli_args['--json-config-file'])
        dataset_experiment = input_config.dataset_experiment
        experiment_name = f'{dataset_experiment.experiment_name}-{str(uuid4())[:8]}'
        dataset_experiment.experiment_name=experiment_name
        logger.debug(f"\n{input_config.format()}")

        rag_query=input_config.rag_query
        client = Langfuse(
            host=str(rag_query.observability_setting.url),
            public_key=rag_query.observability_setting.public_key,
            secret_key=fetch_secret_key_value(rag_query.observability_setting.secret_key),
        )
        dataset = client.get_dataset(dataset_experiment.dataset_name)
        dataset_items = dataset.items

        for item in dataset_items:
            handler = item.get_langchain_handler(
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
                    'document_index_name': rag_query.document_index_name,
                    'k': rag_query.document_search_params.k,
                },
            )

            rag_query.question_answering_prompt.inputs["question"] = item.input["question"]
            await execute_rag_chain(query=rag_query,debug=False, custom_observability_handler=handler)
            tested_items.append(item)

            print(f'Item:{item.id} - Trace:{handler.get_trace_url()}')
            print(f'Waiting for rate limit delay ({input_config.rate_limit_delay}s)...')
            time.sleep(input_config.rate_limit_delay)
        client.flush()
        activity_status = StatusWithReason(status=ActivityStatus.COMPLETED)
    except Exception as e:
        full_exception_name = f"{type(e).__module__}.{type(e).__name__}"
        activity_status = StatusWithReason(status=ActivityStatus.FAILED, status_reason=f"{full_exception_name} : {e}")
        logger.error(e, exc_info=True)

    len_dataset_items = len(dataset_items)
    output = RunExperimentOutput(
        status = activity_status,
        rag_query=rag_query,
        dataset_experiment=dataset_experiment,
        duration = datetime.now() - start_time,
        nb_dataset_items=len(dataset_items),
        success_rate=100 * (len(tested_items) / len_dataset_items) if len_dataset_items > 0 else 0
    )
    logger.debug(f"\n{output.format()}")

if __name__ == '__main__':
    asyncio.run(main())

