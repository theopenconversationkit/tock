#   Copyright (C) 2023-2026 Credit Mutuel Arkea
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
""""
OpenSearch Exception Module
List of all OpenSearch exceptions managed by Gen AI Orchestrator
"""

from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIOrchestratorException,
)
from gen_ai_orchestrator.models.errors.errors_models import (
    ErrorCode,
    ErrorInfo,
)


class GenAIOpenSearchSettingException(GenAIOrchestratorException):
    """The OpenSearch is improperly configured."""

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.OPEN_SEARCH_SETTINGS_ERROR, info)


class GenAIOpenSearchTransportException(GenAIOrchestratorException):
    """
    OpenSearch returns a non-OK (>=400) HTTP status code. Or when
    an actual connection error happens; in that case the status_code will
    be set to N/A.
    """

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.OPEN_SEARCH_TRANSPORT_ERROR, info)


class GenAIOpenSearchIndexNotFoundException(GenAIOrchestratorException):
    """OpenSearch index not found"""

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.OPEN_SEARCH_INDEX_NOT_FOUND, info)


class GenAIOpenSearchResourceNotFoundException(GenAIOrchestratorException):
    """OpenSearch resource not found"""

    def __init__(self, info: ErrorInfo):
        super().__init__(ErrorCode.OPEN_SEARCH_RESOURCE_NOT_FOUND, info)
