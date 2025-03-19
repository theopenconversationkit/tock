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
"""Module for the Observability Service"""

import logging
from typing import Optional

from gen_ai_orchestrator.models.observability.observability_type import ObservabilitySetting
from gen_ai_orchestrator.routers.responses.responses import ObservabilityInfo
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import get_callback_handler_factory
from langfuse.callback import CallbackHandler as LangfuseCallbackHandler

logger = logging.getLogger(__name__)


def check_observability_setting(setting: ObservabilitySetting) -> bool:
    """
    Run a check for a given Observability setting.

    Args:
        setting: The Observability setting to check

    Returns:
         True for a valid Observability setting. Raise exception otherwise.
    """

    logger.info('Get the Callback handler Factory, then check the Observability setting.')
    return get_callback_handler_factory(setting).check_observability_setting()


def get_observability_info(observability_handler) -> Optional[ObservabilityInfo]:
    """Get the observability Information"""
    if isinstance(observability_handler, LangfuseCallbackHandler):
        return ObservabilityInfo(
            trace_id=observability_handler.trace.id,
            trace_name=observability_handler.trace_name,
            trace_url=observability_handler.get_trace_url()
        )
    else:
        return None