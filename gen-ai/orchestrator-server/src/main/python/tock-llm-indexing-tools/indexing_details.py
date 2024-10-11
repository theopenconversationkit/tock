from gen_ai_orchestrator.models.em.em_setting import BaseEMSetting
from gen_ai_orchestrator.models.vector_stores.vector_store_setting import BaseVectorStoreSetting
from pydantic import BaseModel, Field


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
    em_settings: BaseEMSetting = Field(description='The Embeddings settings.')
    vector_store_settings: BaseVectorStoreSetting = Field(description='The Vector Store settings.')
