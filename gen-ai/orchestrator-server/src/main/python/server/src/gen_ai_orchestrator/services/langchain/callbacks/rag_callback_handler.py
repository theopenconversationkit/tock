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
"""Retriever callback handler for LangChain."""

import logging
import time
from typing import Any, Dict

from langchain_core.callbacks import BaseCallbackHandler
from langchain_core.messages import AIMessage, SystemMessage
from langchain_core.prompt_values import ChatPromptValue, StringPromptValue

logger = logging.getLogger(__name__)

TRACKED_STEPS: frozenset = frozenset({
    'document_compressor',
    'similarity_retriever_retrieve',
    'hybrid_retrieve',
    'fts_retrieve',
})


class RAGCallbackHandler(BaseCallbackHandler):
    """Customized RAG callback handler that retrieves data from the chain execution."""

    def __init__(self):
        self._step_timers: dict = {}
        self._llm_call_count: int = 0
        # Guards the one-shot time-to-first-token measurement (streaming only).
        self._first_token_recorded: bool = False
        self.records: Dict[str, Any] = {
            'chat_prompt': None,
            'rag_question_condensation_chain_output': None,
            'rag_prompt': None,
            'rag_chain_output': None,
            'documents': None,
            'step_durations': {},
        }

    def on_chain_start(
        self, serialized: Dict[str, Any], inputs: Dict[str, Any], **kwargs: Any
    ) -> None:
        """Print out that we are entering a chain."""

        if kwargs['name'] == 'rag_question_condensation_chain_output' and isinstance(
            inputs, AIMessage
        ):
            self.records['rag_question_condensation_chain_output'] = inputs.content

        if kwargs['name'] == 'rag_chain_output' and isinstance(inputs, AIMessage):
            self.records['rag_chain_output'] = inputs.content

        if kwargs['name'] == 'RunnableAssign<answer>' and 'documents' in inputs:
            self.records['documents'] = inputs['documents']

        name = kwargs.get('name', '')
        if name in TRACKED_STEPS:
            self._step_timers[str(kwargs['run_id'])] = (name, time.time())

    def on_chain_end(self, outputs: Dict[str, Any], **kwargs: Any) -> None:
        """Print out that we finished a chain."""  # if outputs is instance of StringPromptValue

        if isinstance(outputs, ChatPromptValue):
            self.records['chat_prompt'] = next(
                (
                    msg.content
                    for msg in outputs.messages
                    if isinstance(msg, SystemMessage)
                ),
                None,
            )

        if isinstance(outputs, StringPromptValue):
            self.records['rag_prompt'] = outputs.text

        run_key = str(kwargs.get('run_id', ''))
        if run_key in self._step_timers:
            step_name, start = self._step_timers.pop(run_key)
            self.records['step_durations'][step_name] = round(time.time() - start, 3)

    def on_llm_start(
        self, serialized: Dict[str, Any], prompts: list[str], **kwargs: Any
    ) -> None:
        """Capture LLM call start to measure actual LLM execution time."""
        self._llm_call_count += 1
        run_key = str(kwargs.get('run_id', ''))

        if self._llm_call_count == 1:
            step_name = 'rag_question_condensation_llm'
        elif self._llm_call_count == 2:
            step_name = 'rag_answer_generation_llm'
        else:
            return

        self._step_timers[run_key] = (step_name, time.time())

    def on_llm_new_token(self, token: str, **kwargs: Any) -> None:
        """Record the time-to-first-token of the answer generation (streaming only).

        Fires only when the LLM streams (streaming mode); measures the delay between
        the start of the answer-generation LLM call and its first emitted token, so the
        pure generation latency is isolated from retrieval/condensation.
        """
        if self._first_token_recorded:
            return
        run_key = str(kwargs.get('run_id', ''))
        timer = self._step_timers.get(run_key)
        if timer and timer[0] == 'rag_answer_generation_llm':
            self._first_token_recorded = True
            self.records['step_durations']['rag_answer_generation_first_token'] = (
                '{:.3f}'.format(time.time() - timer[1])
            )

    def on_llm_end(self, response, **kwargs: Any) -> None:
        """Capture LLM call end and record duration."""
        run_key = str(kwargs.get('run_id', ''))
        if run_key in self._step_timers:
            step_name, start = self._step_timers.pop(run_key)
            self.records['step_durations'][step_name] = round(time.time() - start, 3)
