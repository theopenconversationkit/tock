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
from datetime import datetime, timedelta
from typing import List, Optional

import humanize
from pydantic import BaseModel, Field
from scripts.common.models import ActivityOutput, BotInfo, FromJsonMixin

from gen_ai_orchestrator.models.llm.llm_types import LLMSetting
from gen_ai_orchestrator.models.observability.langfuse.langfuse_setting import (
    LangfuseObservabilitySetting,
)


class DocumentChunk(BaseModel):
    id: str = Field(description='The chunk id.')
    content: str = Field(description='The chunk content.')

class RunChunkContextualizationInput(FromJsonMixin):
    bot: BotInfo = Field(description='The bot information.')
    reference_document_name: str = Field(
        description='The document name to use as a reference for contextualization.'
    )
    chunks: List[DocumentChunk] = Field(
        description='The list of chunks to contextualize.',
        default=[]
    )
    llm_setting: Optional[LLMSetting] = Field(
        description='LLM setting, used to contextualize chunks.',
        default=None
    )
    observability_setting: LangfuseObservabilitySetting = Field(
        description='The Langfuse observability settings.'
    )

    def format(self):
        header_text = ' RUN CHUNK CONTEXTUALIZATION INTPUT '
        details_str = f"""
            Langfuse environment   : {str(self.observability_setting.url)}
            The reference document : {self.reference_document_name}
            Number of chunks       : {len(self.chunks)}
            The LLM model          : {self.llm_setting.model} ({self.llm_setting.provider})
            The LLM temperature    : {self.llm_setting.temperature}
        """

        # Find the longest line in the details
        details = details_str.splitlines()
        max_detail_length = max(len(detail) for detail in details)
        # Construct the header and separator lines
        header_line = header_text.center(max_detail_length, '-')
        separator = '-' * max_detail_length

        to_string = f"{header_line}\n{details_str}\n{separator}"
        return '\n'.join(line.strip() for line in to_string.splitlines() if line.strip())

class RunChunkContextualizationOutput(ActivityOutput):
    duration: timedelta = Field(description='The evaluation time.')

    def format(self):
        header_text = ' RUN CHUNK CONTEXTUALIZATION OUTPUT '
        details_str = f"""
        Number of chunks               : {self.items_count}
        Rate of successful evaluations : {self.success_rate:.2f}%
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

class RunImageContextualizationInput(FromJsonMixin):
    bot: BotInfo = Field(description='The bot information.')
    document_directory: str = Field(
        description='The directory name to use as a reference for contextualization.'
    )
    llm_setting: Optional[LLMSetting] = Field(
        description='LLM setting, used to contextualize chunks.',
        default=None
    )
    observability_setting: Optional[LangfuseObservabilitySetting] = Field(
        description='The Langfuse observability settings.',
        default=None
    )

    def format(self):
        header_text = ' RUN IMAGE CONTEXTUALIZATION INTPUT '
        details_str = f"""
            Langfuse environment   : {str(self.observability_setting.url)}
            The document directory : {self.document_directory}
            The LLM model          : {self.llm_setting.model} ({self.llm_setting.provider})
            The LLM temperature    : {self.llm_setting.temperature}
        """

        # Find the longest line in the details
        details = details_str.splitlines()
        max_detail_length = max(len(detail) for detail in details)
        # Construct the header and separator lines
        header_line = header_text.center(max_detail_length, '-')
        separator = '-' * max_detail_length

        to_string = f"{header_line}\n{details_str}\n{separator}"
        return '\n'.join(line.strip() for line in to_string.splitlines() if line.strip())

class RunImageContextualizationOutput(ActivityOutput):
    output_filename: str = Field(description='The output filename.')

    def format(self):
        header_text = ' RUN CHUNK CONTEXTUALIZATION OUTPUT '
        details_str = f"""
        Number of images               : {self.items_count}
        Output filename                : {self.output_filename}
        Rate of successful evaluations : {self.success_rate:.2f}%
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
