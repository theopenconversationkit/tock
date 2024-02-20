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
from unittest.mock import patch

import pytest

from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIUnknownProviderSettingException,
    VectorStoreUnknownException,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_em_factory,
    get_llm_factory,
    get_vector_store_factory,
)


def test_get_llm_factory():
    with pytest.raises(GenAIUnknownProviderSettingException):
        get_llm_factory(setting='settings with incorrect type')


def test_get_em_factory():
    with pytest.raises(GenAIUnknownProviderSettingException):
        get_em_factory(setting='settings with incorrect type')


def test_get_vector_store_factory():
    with pytest.raises(VectorStoreUnknownException):
        get_vector_store_factory(
            vector_store_provider='an incorrect vector store provider',
            embedding_function=None,
            index_name='an index name',
        )
