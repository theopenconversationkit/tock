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
import json
import logging
import math
import time
from typing import List

from langfuse._client.datasets import DatasetItemClient
from langfuse.api import TraceWithFullDetails
from ragas.dataset_schema import SingleTurnSample
from ragas.embeddings import LangchainEmbeddingsWrapper
from ragas.llms import LangchainLLMWrapper
from ragas.metrics.base import MetricWithEmbeddings, MetricWithLLM
from ragas.run_config import RunConfig
from scripts.common.logging_config import configure_logging
from scripts.dataset.evaluation.models import MetricScore, RunEvaluationInput
from scripts.dataset.evaluation.ragas_metrics import ragas_available_metrics

from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    create_observability_callback_handler,
    get_em_factory,
    get_llm_factory,
)

logger = logging.getLogger(__name__)


class RagasEvaluator:
    def __init__(self, langfuse_client, evaluation_input: RunEvaluationInput):
        global logger
        logger = configure_logging([])
        # Initialize LLM and embedding models
        llm_factory = get_llm_factory(setting=evaluation_input.llm_setting)
        em_factory = get_em_factory(setting=evaluation_input.em_setting)

        llm = llm_factory.get_language_model()
        embedding = em_factory.get_embedding_model()

        self.observability_setting = evaluation_input.observability_setting
        self.langfuse_client = langfuse_client

        # Filter selected metrics
        self.metrics = [
            metric
            for metric in ragas_available_metrics
            if metric['name'] in evaluation_input.metric_names
        ]

        # Initialize metrics
        run_config = RunConfig()
        for metric_entry in self.metrics:
            metric = metric_entry['metric']
            if isinstance(metric, MetricWithLLM):
                metric.llm = LangchainLLMWrapper(llm)
            if isinstance(metric, MetricWithEmbeddings):
                metric.embeddings = LangchainEmbeddingsWrapper(embedding)
            metric.init(run_config)
            logger.debug(f"Init run configuration of '{metric.name}'")

    def fetch_statements_reasons(self, trace_id):
        time.sleep(3)  # Waiting for trace update
        trace_full = self.langfuse_client.api.trace.get(trace_id)
        observations = trace_full.observations
        last_gen_item = next(
            (obs for obs in reversed(observations) if obs.type == 'GENERATION'), None
        )
        if last_gen_item and last_gen_item.output:
            parsed_data = json.loads(
                last_gen_item.output['content'].strip('```json').strip('```')
            )
            logger.info(parsed_data.get('statements', []))
            if parsed_data.get('statements', []):
                return ' | '.join(parsed_data['statements'])
        return ''

    def calculate_metric_score(
        self, metric, metric_name, sample, item, experiment_name
    ):
        logger.info(
            f"Calculating {metric_name} score for item '{item.id}' of dataset '{item.dataset_name}' in experiment '{experiment_name}"
        )
        observability_handler = create_observability_callback_handler(
            observability_setting=self.observability_setting, trace_name=metric_name
        )
        score = metric.single_turn_score(sample, callbacks=[observability_handler])
        return (
            -1 if math.isnan(float(score)) else score,
            observability_handler.last_trace_id,
        )

    def score_with_ragas(
        self,
        item: DatasetItemClient,
        run_trace_details: TraceWithFullDetails,
        experiment_name: str,
    ) -> List[MetricScore]:
        query = item.input['question']
        chunks = [doc['page_content'] for doc in run_trace_details.output['documents']]
        answer = run_trace_details.output['answer']
        ground_truth = item.expected_output.get('answer') or ''

        metric_scores: List[MetricScore] = []
        for m in self.metrics:
            metric = m['metric']
            metric_name = m['name']
            sample = SingleTurnSample(
                user_input=query,
                retrieved_contexts=chunks,
                response=answer,
                reference=ground_truth,
            )

            score, trace_id = self.calculate_metric_score(
                metric, metric_name, sample, item, experiment_name
            )

            statements_reasons = (
                'The score is not a number! There must be an error in the metric calculation.'
                if score == -1
                else ''
            )
            if score != -1 and m['hasReason']:
                statements_reasons = self.fetch_statements_reasons(trace_id)

            metric_scores.append(
                MetricScore(
                    metric_name=metric_name,
                    value=score,
                    reason=statements_reasons,
                    trace_id=trace_id,
                )
            )
            logger.info(f"Metric {metric_name} -> score = {score}")

        for metric_score in metric_scores:
            self.langfuse_client.create_score(
                trace_id=run_trace_details.id,
                name=metric_score.metric_name,
                value=metric_score.value,
                comment=' : '.join(
                    filter(None, [metric_score.trace_id, metric_score.reason])
                ),
            )

        return metric_scores
