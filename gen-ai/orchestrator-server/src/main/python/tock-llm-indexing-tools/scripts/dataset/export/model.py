#   Copyright (C) 2025 Credit Mutuel Arkea
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
from datetime import datetime
from typing import List, Optional

import humanize
from pydantic import BaseModel, Field
from scripts.common.models import (
    ActivityOutput,
    BotInfo,
    DatasetTemplate,
    FromJsonMixin,
)
from scripts.dataset.creation.models import DatasetInfo
from scripts.dataset.evaluation.models import DatasetExperiment

from gen_ai_orchestrator.models.observability.langfuse.langfuse_setting import (
    LangfuseObservabilitySetting,
)
from gen_ai_orchestrator.routers.requests.requests import RAGRequest


class DatasetExperimentScore(BaseModel):
    name: str = Field(description='The score name.')
    value: Optional[float] = Field(description='The score value.')
    comment: Optional[str] = Field(description='The score comment.')
    trace_id: str = Field(description='The trace id of score.')


class DatasetExperimentItemRun(BaseModel):
    output: Optional[dict] = Field(description='The output.')
    scores: list[DatasetExperimentScore] = Field(
        description='The list of calculated scores.'
    )
    trace_id: str = Field(description='The trace id of the item run.')
    metadata: dict = Field(description='The run metadata.')


class DatasetExperimentItem(BaseModel):
    input: dict = Field(description='The item input.')
    expected_output: dict = Field(description='The expected output.')
    metadata: dict = Field(description='The item metadata.')
    runs: list[DatasetExperimentItemRun] = Field(description='The item runs.')


class DatasetExperimentData(BaseModel):
    name: str = Field(
        description='The name of the dataset experiment.', default='UNKNOWN'
    )
    items: DatasetExperimentItem = Field(description='The dataset information.')


class DatasetExperimentsData(BaseModel):
    dataset: DatasetInfo = Field(description='The dataset information.')
    experiments: list[DatasetExperimentData] = Field(
        description='Names of experiments in the dataset.', default=[]
    )


class DatasetExperiments(BaseModel):
    dataset_name: str = Field(description='The dataset name.', default='UNKNOWN')
    experiment_names: list[str] = Field(
        description='Names of experiments in the dataset.', default=[]
    )


class ExportExperimentsInput(FromJsonMixin):
    bot: BotInfo = Field(description='The bot information.')
    observability_setting: LangfuseObservabilitySetting = Field(
        description='The Langfuse observability settings.'
    )
    dataset_experiments: DatasetExperiments = Field(
        description='The dataset experiments to export.'
    )
    template: DatasetTemplate = Field(description='The export template.')
    metric_names: List[str] = Field(
        description='The list of RAGAS metric names to be exported.',
        default=['SemanticSimilarity'],
    )

    def format(self):
        header_text = ' EXPORT EXPERIMENTS INPUT '
        details_str = f"""
                Langfuse environment    : {str(self.observability_setting.url)}
                The dataset name        : {self.dataset_experiments.dataset_name}
                The dataset experiments : {" | ".join(self.dataset_experiments.experiment_names)}
                """

        # Find the longest line in the details
        details = details_str.splitlines()
        max_detail_length = max(len(detail) for detail in details)
        # Construct the header and separator lines
        header_line = header_text.center(max_detail_length, '-')
        separator = '-' * max_detail_length

        to_string = f'{header_line}\n{details_str}\n{separator}'
        return '\n'.join(
            line.strip() for line in to_string.splitlines() if line.strip()
        )


class ExportExperimentsOutput(ActivityOutput):
    dataset_name: str = Field(description='The dataset name.')

    def format(self):
        header_text = ' EXPORT EXPERIMENTS OUTPUT '
        details_str = f"""
        The dataset name               : {self.dataset_name}
        Number of items in dataset     : {self.items_count}
        Duration                       : {humanize.precisedelta(self.duration)}
        Date                           : {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}
        """
        status_str = f"""
        Status                         : {self.status.status.name}
      {"Reason                         : " + self.status.status_reason if self.status.status_reason else ""}
        """

        # Find the longest line in the details
        details = details_str.splitlines()
        max_detail_length = max(len(detail) for detail in details)
        # Construct the header and separator lines
        header_line = header_text.center(max_detail_length, '-')
        separator = '-' * max_detail_length

        to_string = (
            f'{header_line}\n{details_str}\n{separator}\n{status_str}\n{separator}'
        )
        return '\n'.join(
            line.strip() for line in to_string.splitlines() if line.strip()
        )
