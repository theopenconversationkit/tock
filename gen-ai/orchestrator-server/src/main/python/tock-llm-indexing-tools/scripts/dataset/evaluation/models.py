from datetime import datetime
from typing import List, Optional

import humanize
from gen_ai_orchestrator.models.em.em_types import EMSetting
from gen_ai_orchestrator.models.llm.llm_types import LLMSetting
from gen_ai_orchestrator.models.observability.langfuse.langfuse_setting import LangfuseObservabilitySetting
from pydantic import BaseModel, Field

from scripts.common.models import ActivityOutput, FromJsonMixin


class DatasetExperiment(BaseModel):
    dataset_name: str = Field(description='The dataset name.', default='UNKNOWN')
    experiment_name: str = Field(description='The name of the dataset experiment.', default='UNKNOWN')
    experiment_description: str = Field(description='The description of the dataset experiment.', default="")


class RunEvaluationInput(FromJsonMixin):
    metric_names: List[str] = Field(
        description='The list of RAGAS metric names.',
        default=["SemanticSimilarity"]
    )
    llm_setting: Optional[LLMSetting] = Field(
        description='LLM setting, used to calculate the LLM-based metric.',
        default=None
    )
    em_setting: Optional[EMSetting] = Field(
        description='Embeddings setting, used to calculate the Embeddings-based metric.',
        default=None
    )
    observability_setting: LangfuseObservabilitySetting = Field(
        description='The Langfuse observability settings.'
    )
    dataset_experiment: DatasetExperiment = Field(
        description='The dataset experiment to evaluate.'
    )

    def format(self):
        header_text = " RUN EVALUATION INPUT "
        details_str = f"""
            Langfuse environment : {str(self.observability_setting.url)}
            The dataset name     : {self.dataset_experiment.dataset_name}
            The experiment name  : {self.dataset_experiment.experiment_name}
            Metrics              : {" | ".join(self.metric_names)}
        """

        # Find the longest line in the details
        details = details_str.splitlines()
        max_detail_length = max(len(detail) for detail in details)
        # Construct the header and separator lines
        header_line = header_text.center(max_detail_length, '-')
        separator = '-' * max_detail_length

        to_string = f"{header_line}\n{details_str}\n{separator}"
        return "\n".join(line.strip() for line in to_string.splitlines() if line.strip())


class MetricScore(BaseModel):
    metric_name: str = Field(description='The metric name.')
    value: float = Field(description='The metric value.')
    reason: str = Field(description='The reason for the score value.')
    trace_id: str = Field(description='The trace ID for metric calculation.')

class DatasetExperimentItemScores(BaseModel):
    run_item_id: str = Field(description='The item ID in the dataset experience.')
    run_trace_id: str = Field(description='The trace ID for the item in the dataset experience.')
    metric_scores: List[MetricScore] = Field(
        description='The metric scores.',
        default=["SemanticSimilarity"]
    )

class RunEvaluationOutput(ActivityOutput):
    dataset_experiment: DatasetExperiment = Field(
        description='The dataset experiment to evaluate.'
    )
    dataset_experiment_scores: List[DatasetExperimentItemScores] = Field(
        description='Scores for the dataset experiment.'
    )

    def format(self):
        header_text = " RUN EVALUATION OUTPUT "
        details_str = f"""
        The dataset name               : {self.dataset_experiment.dataset_name}
        The experiment name            : {self.dataset_experiment.experiment_name}
        Number of items in dataset     : {self.nb_dataset_items}
        Rate of successful evaluations : {self.success_rate:.2f}%
        Duration                       : {humanize.precisedelta(self.duration)}
        Date                           : {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}
        """
        status_str = f"""
        Status                         : {self.status.status.name}
        Reason                         : {self.status.status_reason}
        """

        # Find the longest line in the details
        details = details_str.splitlines()
        max_detail_length = max(len(detail) for detail in details)
        # Construct the header and separator lines
        header_line = header_text.center(max_detail_length, '-')
        separator = '-' * max_detail_length

        to_string = f"{header_line}\n{details_str}\n{separator}\n{status_str}\n{separator}"
        return "\n".join(line.strip() for line in to_string.splitlines() if line.strip())


