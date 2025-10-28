#   Copyright (C) 2023-2025 Credit Mutuel Arkea
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
from typing import Any, Dict

from langchain.callbacks.base import BaseCallbackHandler
from langchain_core.messages import AIMessage, SystemMessage
from langchain_core.prompt_values import ChatPromptValue, StringPromptValue

logger = logging.getLogger(__name__)


class RAGCallbackHandler(BaseCallbackHandler):
    """Customized RAG callback handler that retrieves data from the chain execution."""

    def __init__(self):
        self.records: Dict[str, Any] = {
            'chat_prompt': None,
            'chat_chain_output': None,
            'rag_prompt': None,
            'rag_chain_output': None,
            'documents': None,
        }

    def on_chain_start(
        self, serialized: Dict[str, Any], inputs: Dict[str, Any], **kwargs: Any
    ) -> None:
        """Print out that we are entering a chain."""

        if kwargs['name'] == 'chat_chain_output' and isinstance(inputs, AIMessage):
            self.records['chat_chain_output'] = inputs.content

        if kwargs['name'] == 'rag_chain_output' and isinstance(inputs, AIMessage):
            self.records['rag_chain_output'] = inputs.content

        if kwargs['name'] == 'RunnableAssign<answer>' and 'documents' in inputs:
            self.records['documents'] = inputs['documents']

    def on_chain_end(self, outputs: Dict[str, Any], **kwargs: Any) -> None:
        """Print out that we finished a chain.""" # if outputs is instance of StringPromptValue

        if isinstance(outputs, ChatPromptValue):
            self.records['chat_prompt'] = next(
                (msg.content for msg in outputs.messages if isinstance(msg, SystemMessage)), None
            )

        if isinstance(outputs, StringPromptValue):
            self.records['rag_prompt'] = outputs.text
