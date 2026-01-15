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
from datetime import datetime
from typing import Optional

import humanize
from pydantic import BaseModel, Field
from scripts.common.models import (
    ActivityOutput,
    BotInfo,
    DatasetTemplate,
    FromJsonMixin,
)

from gen_ai_orchestrator.models.observability.langfuse.langfuse_setting import (
    LangfuseObservabilitySetting,
)


class DatasetInfo(BaseModel):
    name: str = Field(description='The dataset name.')
    description: str = Field(description='The dataset description.', default='')
    metadata: dict = Field(description='The dataset metadata.', default={})
    template: DatasetTemplate = Field(description='The dataset template.')

class DatasetItemInfo(BaseModel):
    topic: Optional[str] = Field(description='The question topic.', default=None)
    question: str = Field(description='The question.')
    answer: str = Field(description='The expected answer.')

class CreateDatasetInput(FromJsonMixin):
    bot: BotInfo = Field(description='The bot information.')
    dataset: DatasetInfo = Field(description='The dataset information.')
    observability_setting: LangfuseObservabilitySetting = Field(
        description='The Langfuse observability settings.'
    )

    def format(self):
        header_text = ' CREATE DATASET INPUT '
        details_str = f"""
        Langfuse environment : {str(self.observability_setting.url)}
        The dataset name     : {self.dataset.name}
        The dataset template : {self.dataset.template.file}
        """

        # Find the longest line in the details
        details = details_str.splitlines()
        max_detail_length = max(len(detail) for detail in details)
        # Construct the header and separator lines
        header_line = header_text.center(max_detail_length, '-')
        separator = '-' * max_detail_length

        to_string = f"{header_line}\n{details_str}\n{separator}"
        return '\n'.join(line.strip() for line in to_string.splitlines() if line.strip())


class CreateDatasetOutput(ActivityOutput):
    dataset_name: str = Field(description='The dataset name.')

    def format(self):
        header_text = ' CREATE DATASET OUTPUT '
        details_str = f"""
        The dataset name               : {self.dataset_name}
        Number of items in dataset     : {self.items_count}
        Rate of successful experiment  : {self.success_rate:.2f}%
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

        to_string = f"{header_line}\n{details_str}\n{separator}\n{status_str}\n{separator}"
        return '\n'.join(line.strip() for line in to_string.splitlines() if line.strip())
