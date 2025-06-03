from datetime import datetime
from typing import Optional

import humanize
from gen_ai_orchestrator.routers.requests.requests import RagQuery
from pydantic import Field

from scripts.common.models import FromJsonMixin, BotInfo, ActivityOutput
from scripts.dataset.evaluation.models import DatasetExperiment


class RunExperimentInput(FromJsonMixin):
    bot: BotInfo = Field(description='The bot information.')
    rag_query: RagQuery = Field(
        description='The RAG query.'
    )
    dataset_experiment: DatasetExperiment = Field(
        description='The dataset to experiment.'
    )
    rate_limit_delay: int = Field(
        description='Waiting time (in seconds) to prevent the LLM rate limite.'
    )
    export_settings: Optional[dict] = Field(
        default=None,
        description="Optional export parameters (exclude keys)"
    )


    def format(self):
        header_text = " RUN EXPERIMENT INPUT "
        details_str = f"""
        Langfuse environment : {str(self.rag_query.observability_setting.url)}
        The dataset name     : {self.dataset_experiment.dataset_name}
        The experiment name  : {self.dataset_experiment.experiment_name}
        Rate limit delay     : {self.rate_limit_delay}s
        """

        # Find the longest line in the details
        details = details_str.splitlines()
        max_detail_length = max(len(detail) for detail in details)
        # Construct the header and separator lines
        header_line = header_text.center(max_detail_length, '-')
        separator = '-' * max_detail_length

        to_string = f"{header_line}\n{details_str}\n{separator}"
        return "\n".join(line.strip() for line in to_string.splitlines() if line.strip())


class RunExperimentOutput(ActivityOutput):
    rag_query: Optional[RagQuery] = Field(
        description='The RAG query.'
    )
    dataset_experiment: DatasetExperiment = Field(
        description='The dataset to experiment.'
    )

    items_count: int = Field(description='Number of items in dataset.')

    def format(self):
        header_text = " RUN EXPERIMENT OUTPUT "
        details_str = f"""
        The dataset name               : {self.dataset_experiment.dataset_name}
        The experiment name            : {self.dataset_experiment.experiment_name}
        Number of items in dataset     : {self.items_count}
        Rate of successful experiment  : {self.success_rate:.2f}%
        Duration                       : {humanize.precisedelta(self.duration)}
        Date                           : {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}
        """
        rag_config_str = f"""
        The Vector store               : {self.rag_query.vector_store_setting.provider}
        The document index name        : {self.rag_query.document_index_name}
        The knn                        : {self.rag_query.document_search_params.k}
        The LLM model                  : {self.rag_query.question_answering_llm_setting.model} ({self.rag_query.question_answering_llm_setting.provider})
        The LLM temperature            : {self.rag_query.question_answering_llm_setting.temperature}
        The EM model                   : {self.rag_query.embedding_question_em_setting.model} ({self.rag_query.embedding_question_em_setting.provider})
        """
        status_str = f"""
        Status                         : {self.status.status.name}
      {"Reason                         : " + self.status.status_reason if self.status.status_reason else ""}
        """

        # Find the longest line in the details
        details = (details_str + rag_config_str).splitlines()
        max_detail_length = max(len(detail) for detail in details)
        # Construct the header and separator lines
        header_line = header_text.center(max_detail_length, '-')
        separator = '-' * max_detail_length

        to_string = f"{header_line}\n{details_str}\n{separator}\n{rag_config_str}\n{separator}\n{status_str}\n{separator}"
        return "\n".join(line.strip() for line in to_string.splitlines() if line.strip())