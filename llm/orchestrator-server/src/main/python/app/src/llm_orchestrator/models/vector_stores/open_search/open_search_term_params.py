#   Copyright (C) 2024 Credit Mutuel Arkea
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

from pydantic import BaseModel, Field

from llm_orchestrator.models.vector_stores.vector_store_search_params import (
    BaseVectorStoreSearchParams,
)
from llm_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)


class OpenSearchTermParams(BaseModel):
    term: dict = Field(
        description='A key-value object',
        examples=[{'metadata.id': 'abc-123'}],
        default={},
    )
