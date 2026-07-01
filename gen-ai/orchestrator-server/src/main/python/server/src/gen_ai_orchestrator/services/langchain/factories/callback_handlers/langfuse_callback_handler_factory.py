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
"""Model for creating Langfuse Callback Handler Factory"""

import logging
from typing import Any, Optional

from langfuse import Langfuse
from langfuse.api.core import ApiError
from langfuse.langchain import CallbackHandler as LangfuseCallbackHandler
from pydantic import PrivateAttr

from gen_ai_orchestrator.configurations.environment.settings import (
    application_settings,
)
from gen_ai_orchestrator.errors.exceptions.observability.observability_exceptions import (
    GenAIObservabilityErrorException,
)
from gen_ai_orchestrator.errors.handlers.langfuse.langfuse_exception_handler import (
    create_error_info_langfuse,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo
from gen_ai_orchestrator.models.observability.observability_trace import (
    ObservabilityTrace,
)
from gen_ai_orchestrator.models.observability.observability_type import (
    ObservabilitySetting,
)
from gen_ai_orchestrator.services.langchain.factories.callback_handlers.callback_handlers_factory import (
    LangChainCallbackHandlerFactory,
)
from gen_ai_orchestrator.services.security.security_service import (
    fetch_secret_key_value,
)

logger = logging.getLogger(__name__)


class LangfuseCallbackHandlerFactory(LangChainCallbackHandlerFactory):
    """A class for Langfuse Callback Handler Factory"""

    setting: ObservabilitySetting

    # Internal client cache
    _langfuse_client: Optional[Langfuse] = PrivateAttr(default=None)

    def _get_langfuse_client(self) -> Langfuse:
        """
        Create or return the initialized Langfuse client
        """
        if self._langfuse_client is None:
            self._langfuse_client = Langfuse(
                public_key=self.setting.public_key,
                secret_key=fetch_secret_key_value(self.setting.secret_key),
                base_url=str(self.setting.url),
                timeout=application_settings.observability_provider_timeout,
            )
        return self._langfuse_client

    def get_callback_handler(self, **kwargs: Any) -> LangfuseCallbackHandler:
        """
        Create Langfuse CallbackHandler
        """
        self._get_langfuse_client()

        # Langfuse SDK maintains an internal map / pool of clients based on there public key, that why the client isn't passed to the callbackhandler constructor.
        return LangfuseCallbackHandler(
            public_key=self.setting.public_key,
        )

    def check_observability_setting(self) -> bool:
        """Check if the provided credentials (public and secret key) are valid,
        while tracing a sample phrase"""
        try:
            client = self._get_langfuse_client()
            logger.debug('Langfuse client initialized.')

            if not client.auth_check():
                raise GenAIObservabilityErrorException(
                    ErrorInfo(
                        error='Langfuse authentication check failed',
                        cause='API Keys',
                    )
                )

            with client.start_as_current_observation(
                as_type='span',
                name=ObservabilityTrace.CHECK_OBSERVABILITY_SETTINGS.value,
                input={'message': 'Check observability setting'},
            ) as span:
                span.update(output='Check observability setting trace')

            client.flush()

        except ApiError as exc:
            logger.error(exc)
            raise GenAIObservabilityErrorException(create_error_info_langfuse(exc))
        return True
