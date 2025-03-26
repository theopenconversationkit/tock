from datetime import datetime, timedelta
from pathlib import Path
from typing import Optional

import humanize
from gen_ai_orchestrator.models.em.em_setting import BaseEMSetting
from gen_ai_orchestrator.models.em.em_types import EMSetting
from gen_ai_orchestrator.models.vector_stores.vector_store_setting import BaseVectorStoreSetting
from gen_ai_orchestrator.models.vector_stores.vector_store_types import VectorStoreSetting
from pydantic import Field, BaseModel

from scripts.common.models import ActivityOutput, FromJsonMixin, BotInfo


class RunVectorisationInput(FromJsonMixin):
    bot: BotInfo = Field(description='The bot information.')
    em_setting: EMSetting = Field(description='Embeddings setting')
    vector_store_setting: VectorStoreSetting = Field(description='The vector store settings.')
    data_csv_file: Path = Field(description='The csv file path.')
    document_index_name: Optional[str] = Field(description='Index name to create.')
    chunk_size: int = Field(description='The chunk size (number of characters).', ge=500)
    embedding_bulk_size: int = Field(description='The embedding bulk size.', ge=1)
    ignore_source: bool = Field(description='Ignore source url if True.')
    append_doc_title_and_chunk: bool = Field(description='Append title and chunk before vectorisation if True.')

    def format(self):
        header_text = " RUN VECTORISATION INPUT "
        details_str = f"""
                Bot                     : {self.bot.namespace} - {self.bot.bot_id}
                The EM model            : {self.em_setting.model} ({self.em_setting.provider})
                The Vector DB           : {self.vector_store_setting.host} ({self.vector_store_setting.provider})
                The data csv path       : {self.data_csv_file}
                Chunk size              : {self.chunk_size}
                Embedding bulk size     : {self.embedding_bulk_size}
                Ignoring sources        : {self.ignore_source}
                Append title and chunk  : {self.append_doc_title_and_chunk}
                """

        # Find the longest line in the details
        details = details_str.splitlines()
        max_detail_length = max(len(detail) for detail in details)
        # Construct the header and separator lines
        header_line = header_text.center(max_detail_length, '-')
        separator = '-' * max_detail_length

        to_string = f"{header_line}\n{details_str}\n{separator}"
        return "\n".join(line.strip() for line in to_string.splitlines() if line.strip())

class RunVectorisationOutput(ActivityOutput):
    index_name: str = Field(
        description='The full index name.',
        examples=["ns_03_bot_cmso_session_6f7a7023_ef29_448a_ba33_44ec2e21cd32"]
    )
    session_uuid: str = Field(
        description='The indexing session unique id.',
        examples=["6f7a7023_ef29_448a_ba33_44ec2e21cd32"]
    )
    chunks_count: int = Field(
        description='Number of chunked documents.',
        examples=[81033]
    )

    def format(self):
        header_text = " RUN VECTORISATION OUTPUT "
        details_str = f"""
        Index name             : {self.index_name}
        Index session ID       : {self.session_uuid}
        Documents extracted    : {self.items_count} (Docs)
        Documents chunked      : {self.chunks_count} (Chunks)
        Duration               : {humanize.precisedelta(self.duration)}
        Date                   : {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}
        """
        status_str = f"""
        Status                 : {self.status.status.name}
      {"Reason                 : " + self.status.status_reason if self.status.status_reason else ""}
        """

        # Find the longest line in the details
        details = details_str.splitlines()
        max_detail_length = max(len(detail) for detail in details)
        # Construct the header and separator lines
        header_line = header_text.center(max_detail_length, '-')
        separator = '-' * max_detail_length

        to_string = f"{header_line}\n{details_str}\n{separator}\n{status_str}\n{separator}"
        return "\n".join(line.strip() for line in to_string.splitlines() if line.strip())

class IndexingDetails(BaseModel):
    """The indexing detail base class for Embedding Model Setting."""

    index_name: str = Field(
        description='The full index name.',
        examples=["ns_03_bot_cmso_session_6f7a7023_ef29_448a_ba33_44ec2e21cd32"]
    )
    indexing_session_uuid: str = Field(
        description='The indexing session unique id.',
        examples=["6f7a7023_ef29_448a_ba33_44ec2e21cd32"]
    )
    documents_count: int = Field(
        description='Number of documents read from csv input file.',
        examples=[39271]
    )
    chunks_count: int = Field(
        description='Number of chunked documents.',
        examples=[81033]
    )
    chunk_size: int = Field(
        description='The chunk size.',
        examples=[1000]
    )
    input_csv: str = Field(description='The input csv file name.')
    em_settings: BaseEMSetting = Field(description='The Embeddings settings.')
    vector_store_settings: BaseVectorStoreSetting = Field(description='The Vector Store settings.')
    ignore_source: bool = Field(description='Boolean to ignore or not document sources.')
    duration: timedelta = Field(description='The indexing execution time.')


    def format_indexing_details(self):
        # Format the details string
        details_str = f"""
Index name          : {self.index_name}
Index session ID    : {self.indexing_session_uuid}
Documents extracted : {self.documents_count} (Docs)
Documents chunked   : {self.chunks_count} (Chunks)
Chunk size          : {self.chunk_size} (Characters)
Input csv           : {self.input_csv}
Embeddings          : {self.em_settings.provider}
Vector Store        : {self.vector_store_settings.provider}
Ignoring sources    : {self.ignore_source}
Duration            : {humanize.precisedelta(self.duration)}
Date                : {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}
"""
