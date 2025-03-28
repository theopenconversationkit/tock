import json
from datetime import datetime
from datetime import timedelta
from typing import List, Optional

import humanize
from gen_ai_orchestrator.models.llm.llm_types import LLMSetting
from gen_ai_orchestrator.models.observability.langfuse.langfuse_setting import LangfuseObservabilitySetting
from pydantic import BaseModel, Field

from scripts.common.models import ActivityOutput, FromJsonMixin, BotInfo


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
        header_text = " RUN CHUNK CONTEXTUALIZATION INTPUT "
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
        return "\n".join(line.strip() for line in to_string.splitlines() if line.strip())

class RunChunkContextualizationOutput(ActivityOutput):
    duration: timedelta = Field(description='The evaluation time.')

    def format(self):
        header_text = " RUN CHUNK CONTEXTUALIZATION OUTPUT "
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
        return "\n".join(line.strip() for line in to_string.splitlines() if line.strip())

class RunImageContextualizationInput(FromJsonMixin):
    bot: BotInfo = Field(description='The bot information.')
    reference_document_directory: str = Field(
        description='The directory containing md document and images.'
    )
    reference_document_name: str = Field(
        description='The document name to use as a reference for contextualization.'
    )
    llm_setting: Optional[LLMSetting] = Field(
        description='LLM setting, used to contextualize chunks.',
        default=None
    )
    observability_setting: LangfuseObservabilitySetting = Field(
        description='The Langfuse observability settings.'
    )
    rate_limit_delay: int = Field(
        description='Waiting time (in seconds) to prevent the LLM rate limite.'
    )

    def format(self):
        header_text = " RUN IMAGE CONTEXTUALIZATION INTPUT "
        details_str = f"""
            Langfuse environment   : {str(self.observability_setting.url)}
            The reference document : {self.reference_document_name}
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
        return "\n".join(line.strip() for line in to_string.splitlines() if line.strip())

class RunImageContextualizationOutput(ActivityOutput):
    duration: timedelta = Field(description='The evaluation time.')

    def format(self):
        header_text = " RUN CHUNK CONTEXTUALIZATION OUTPUT "
        details_str = f"""
        Number of images               : {self.items_count}
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
        return "\n".join(line.strip() for line in to_string.splitlines() if line.strip())
