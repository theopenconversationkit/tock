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
"""Completion (Playground) callback handler for LangChain."""

import logging
import time
from typing import Any, Dict, Optional

from langchain_core.callbacks import BaseCallbackHandler

logger = logging.getLogger(__name__)


class CompletionCallbackHandler(BaseCallbackHandler):
    """Lightweight callback handler measuring the time-to-first-token (TTFT)
    of the completion generation, in streaming mode only.

    Mirrors the TTFT logic of RAGCallbackHandler, but the playground chain
    performs a single LLM call, so no step disambiguation is needed.
    """

    def __init__(self):
        self._start_time: Optional[float] = None
        # Guards the one-shot time-to-first-token measurement (streaming only).
        self._first_token_recorded: bool = False
        self.ttft: Optional[float] = None

    def on_llm_start(
        self, serialized: Dict[str, Any], prompts: list[str], **kwargs: Any
    ) -> None:
        """Capture the LLM call start to measure actual generation latency."""
        self._start_time = time.time()

    def on_llm_new_token(self, token: str, **kwargs: Any) -> None:
        """Record the time-to-first-token of the generation (streaming only).

        Fires only when the LLM streams; measures the delay between the start of
        the LLM call and its first emitted token.
        """
        if self._first_token_recorded or self._start_time is None:
            return
        self._first_token_recorded = True
        self.ttft = float('{:.3f}'.format(time.time() - self._start_time))
