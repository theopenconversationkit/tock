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
"""Main module to create and launch FastAPI application"""

import logging

from fastapi import FastAPI

from gen_ai_orchestrator.configurations.logging.logger import setup_logging
from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIOrchestratorException,
)
from gen_ai_orchestrator.errors.handlers.fastapi.fastapi_handler import (
    business_exception_handler,
    generic_exception_handler,
)
from gen_ai_orchestrator.routers.app_monitors_router import (
    application_check_router,
)
from gen_ai_orchestrator.routers.completion_router import completion_router
from gen_ai_orchestrator.routers.em_providers_router import em_providers_router
from gen_ai_orchestrator.routers.llm_providers_router import llm_providers_router
from gen_ai_orchestrator.routers.observability_providers_router import observability_providers_router
from gen_ai_orchestrator.routers.qa_router import qa_router
from gen_ai_orchestrator.routers.rag_router import rag_router
from gen_ai_orchestrator.routers.vector_store_providers_router import vector_store_providers_router

# configure logging
setup_logging()
logger = logging.getLogger(__name__)
logger.info('Logging configuration completed')

logger.info('Generative AI Orchestrator - Starting...')
app = FastAPI(title='Generative AI Orchestrator')

# Add functional exception handler
logger.info('Generative AI Orchestrator - Add exception handlers')
app.add_exception_handler(GenAIOrchestratorException, business_exception_handler)
app.add_exception_handler(Exception, generic_exception_handler)

logger.info('Generative AI Orchestrator - Add routers')
app.include_router(application_check_router)
app.include_router(llm_providers_router)
app.include_router(em_providers_router)
app.include_router(vector_store_providers_router)
app.include_router(observability_providers_router)
app.include_router(rag_router)
app.include_router(qa_router)
app.include_router(completion_router)

logger.info('Generative AI Orchestrator - Startup')
logger.debug('Generative AI Orchestrator - Startup - Debug logs enabled')
