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
"""RAG Router Module"""
import logging

from fastapi import APIRouter
from fastapi import Request

from gen_ai_orchestrator.configurations.environment.settings import application_settings
from gen_ai_orchestrator.errors.exceptions.ai_provider.ai_provider_exceptions import AIProviderBadQueryException
from gen_ai_orchestrator.errors.handlers.fastapi.fastapi_handler import create_error_info_bad_request
from gen_ai_orchestrator.models.vector_stores.vector_store_types import VectorStoreSetting, DocumentSearchParams
from gen_ai_orchestrator.routers.requests.requests import RagQuery
from gen_ai_orchestrator.routers.responses.responses import RagResponse
from gen_ai_orchestrator.services.rag.rag_service import rag

logger = logging.getLogger(__name__)

rag_router = APIRouter(prefix='/rag', tags=['Retrieval Augmented Generation'])


@rag_router.post('')
async def ask_rag(request: Request, query: RagQuery, debug: bool = False) -> RagResponse:
    """
    ## Ask a RAG System
    Ask question to a RAG System, and return answer by using a knowledge base (documents)
    """
    # Check the consistency of the Vector Store Provider with the request body
    validate_vector_store_rag_query(request, query.vector_store_setting, query.document_search_params)

    # execute RAG
    return await rag(query, debug)

def validate_vector_store_rag_query(
        request: Request,
        vector_store_setting: VectorStoreSetting,
        vector_store_search_params: DocumentSearchParams):
    """
    Check the consistency of the Vector Store Provider with the request body
    Args:
        request: The http request
        vector_store_setting:  The Vector Store Provider Setting
        vector_store_search_params: the vector store search params

    Raises:
        AIProviderBadQueryException: if the search parameters are not compatible with the vector store
    """

    logger.debug('RAG - Query validation')

    vector_store_provider = application_settings.vector_store_provider
    if vector_store_setting is not None:
        vector_store_provider = vector_store_setting.provider

    if vector_store_provider != vector_store_search_params.provider:
        logger.error('Inconsistency between vector store provider and document search parameters (%s Vs %s)',
                     vector_store_provider.value, vector_store_search_params.provider.value)
        raise AIProviderBadQueryException(
            create_error_info_bad_request(
                request=request,
                provider=vector_store_search_params.provider,
                cause='Inconsistency between vector store provider and document search parameters')
        )
