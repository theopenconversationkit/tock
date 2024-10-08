#   Copyright (C) 2023-2024 Credit Mutuel Arkea
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
"""Model for creating OpenSearchParams."""

from typing import List, Literal, Optional

from pydantic import Field

from gen_ai_orchestrator.models.vector_stores.open_search.open_search_term_params import OpenSearchTermParams
from gen_ai_orchestrator.models.vector_stores.vector_store_search_params import BaseVectorStoreSearchParams
from gen_ai_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)


class OpenSearchParams(BaseVectorStoreSearchParams):
    """The OpenSearch params. Used to perform a Boolean query.
    https://opensearch.org/docs/latest/query-dsl/compound/bool/"""

    provider: Literal[VectorStoreProvider.OPEN_SEARCH] = Field(
        description='The Vector Store Provider.',
        examples=[VectorStoreProvider.OPEN_SEARCH],
        default=VectorStoreProvider.OPEN_SEARCH,
    )
    filter: Optional[List[OpenSearchTermParams]] = Field(
        description='The OpenSearch boolean query filter. Logical "and" operator is applied. For more information, '
        'see : https://opensearch.org/docs/latest/query-dsl/compound/bool/',
        examples=[[{'term': {'key_1': 'value_1'}}, {'term': {'key_2': 'value_2'}}]],
        default=None
    )

    def to_dict(self):
        result = {'k': self.k}

        if self.filter:
            result['filter'] = [{'term': term_param.term} for term_param in self.filter]

        return result

