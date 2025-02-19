import json
from datetime import datetime
from datetime import timedelta
from typing import List, Optional

import humanize
from gen_ai_orchestrator.models.llm.llm_types import LLMSetting
from gen_ai_orchestrator.models.observability.langfuse.langfuse_setting import LangfuseObservabilitySetting
from pydantic import BaseModel, Field

from scripts.common.models import OutputStatus


class DocumentChunk(BaseModel):
    id: str = Field(description='The chunk id.')
    content: str = Field(description='The chunk content.')


class ReferenceDocument(BaseModel):
    name: str = Field(description='The document name.')
    content: str = Field(description='The document content.')

class RunChunkContextualizationInput(BaseModel):
    document: ReferenceDocument = Field(
        description='The document to use as a reference for chunks.'
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

    @classmethod
    def from_json_file(cls, file_path: str):
        try:
            with open(file_path, "r", encoding="utf-8") as f:
                data = json.load(f)
            return cls(**data)
        except FileNotFoundError:
            raise ValueError(f"The file '{file_path}' is not found!")
        except json.JSONDecodeError:
            raise ValueError(f"the file '{file_path}' is not a valid JSON!")

    def format(self):
        # Format the details string
        details_str = f"""
            Langfuse environment   : {str(self.observability_setting.url)}
            The reference document : {self.document.name}
            Number of chunks       : {len(self.chunks)}
            The LLM model          : {self.llm_setting.model} ({self.llm_setting.provider})
            The LLM temperature    : {self.llm_setting.temperature}
        """

        # Find the longest line in the details
        lines = details_str.splitlines()
        max_line_length = max(len(line) for line in lines)

        # The text for the header
        header_text = " RUN CHUNK CONTEXTUALIZATION INTPUT "

        # Calculate the number of dashes needed on both sides
        total_dashes = max_line_length - len(header_text)
        left_dashes = total_dashes // 2
        right_dashes = total_dashes - left_dashes

        # Construct the header and separator lines
        separator = '-' * max_line_length
        header_line = '-' * left_dashes + header_text + '-' * right_dashes

        # Return the formatted string
        to_string = f"{header_line}\n{details_str}\n{separator}"
        return "\n".join(line.strip() for line in to_string.splitlines() if line.strip())


class RunChunkContextualizationOutput(OutputStatus):

    duration: timedelta = Field(description='The evaluation time.')
    nb_chunks: int = Field(description='Number of chunks.') # TODO MASS : a factoriser
    success_rate: float = Field(description='Rate of successful contextualization.')

    def format(self):
        # Format the details string
        details_str = f"""
            Number of chunks               : {self.nb_chunks}
            Rate of successful evaluations : {self.pass_rate:.2f}%
            Duration                       : {humanize.precisedelta(self.duration)}
            Date                           : {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}
        """

        # Find the longest line in the details
        lines = details_str.splitlines()
        max_line_length = max(max(len(line.strip()) for line in details_str.splitlines()),
                              len(f" REASON: {self.status.status_reason} "))

        # The text for the header
        header_text = " RUN CHUNK CONTEXTUALIZATION OUTPUT "

        # Construct the header and separator lines
        separator = '-' * max_line_length
        header_line = header_text.center(max_line_length, '-')

        # Format status line
        status_line = f" STATUS: {self.status.status.name} ".center(max_line_length)

        if self.status.status_reason:
            status_reason_line = f" REASON: {self.status.status_reason} ".center(max_line_length)
            status_line = f'{status_line}\n{status_reason_line}'

        # Return the formatted string
        to_string = f"{header_line}\n{details_str}\n{separator}\n"
        to_string_strip = "\n".join(line.strip() for line in to_string.splitlines() if line.strip())
        return f'{to_string_strip}\n{status_line}\n{separator}'

