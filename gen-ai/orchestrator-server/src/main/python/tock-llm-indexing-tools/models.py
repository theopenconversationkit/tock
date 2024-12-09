from datetime import datetime, timedelta
from typing import Optional

import humanize
from gen_ai_orchestrator.models.em.em_setting import BaseEMSetting
from gen_ai_orchestrator.models.vector_stores.vector_store_setting import BaseVectorStoreSetting
from pydantic import BaseModel, Field

class DatasetItem(BaseModel):
    """The dataset item"""
    topic: Optional[str] = Field(description='The question topic.', examples=["Security"])
    question: Optional[str] = Field(description='The question.', examples=["How do I secure my application?"])
    locale: Optional[str] = Field(description='The question language.', examples=["French"])
    answer: Optional[str] = Field(description='The answer.', examples=["No way to do that!"])
    no_answer: Optional[str] = Field(description='The default answer if no answer.', examples=["Sorry! I don't know."])

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

        # Find the longest line in the details
        lines = details_str.splitlines()
        max_line_length = max(len(line) for line in lines)

        # The text for the header
        header_text = " Indexing details "

        # Calculate the number of dashes needed on both sides
        total_dashes = max_line_length - len(header_text)
        left_dashes = total_dashes // 2
        right_dashes = total_dashes - left_dashes

        # Construct the header and separator lines
        separator = '-' * max_line_length
        header_line = '-' * left_dashes + header_text + '-' * right_dashes

        # Return the formatted string
        return f"""
{header_line}
{details_str}
{separator}
"""
