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
"""Module for the LangChain Vector Store Factory"""

from abc import ABC, abstractmethod

from langchain_core.embeddings import Embeddings
from langchain_core.vectorstores import VectorStore
from pydantic import BaseModel, ConfigDict


class LangChainVectorStoreFactory(ABC, BaseModel):
    """A base class for LangChain Vector Store Factory"""

    embedding_function: Embeddings
    index_name: str
    model_config = ConfigDict(arbitrary_types_allowed=True)

    @abstractmethod
    def get_vector_store(self) -> VectorStore:
        """
        Fabric the Vector Store.
        :return: VectorStore the interface for Vector Database.
        """
        pass
