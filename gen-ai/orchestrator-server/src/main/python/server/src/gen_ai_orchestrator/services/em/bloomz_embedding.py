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

import logging
from typing import List, Union
from urllib.parse import urljoin

import requests
from langchain.schema.embeddings import Embeddings
from pydantic import BaseModel

logger = logging.getLogger(__name__)


class InferenceRequest(BaseModel):
    text: Union[str, list]
    pooling: str


class BloomzEmbeddings(BaseModel, Embeddings):
    """A class for Bloomz Embeddings Model"""

    pooling: str
    api_base: str

    @property
    def _api_url(self) -> str:
        return urljoin(self.api_base, '/embed')

    def embed_documents(self, texts: List[str]) -> List[List[float]]:
        """Get the embeddings for a list of texts."""
        response = requests.post(
            self._api_url,
            json=InferenceRequest(text=texts, pooling=self.pooling).model_dump(
                mode='json'
            ),
            verify=False,
        )
        if response.status_code != 200:
            logger.exception(
                f"Embedding request didn't return expected status code {response.content}"
            )
        return response.json()['embedding']

    def embed_query(self, text: str) -> List[float]:
        """Compute query embeddings using a HuggingFace transformer model."""
        return self.embed_documents([text])[0]
