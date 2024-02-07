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
"""Retriever callback handler for LangChain."""

import logging
from typing import Any, Dict, List, Optional, Union

from langchain.callbacks.base import BaseCallbackHandler
from langchain.schema import AgentAction, AgentFinish, LLMResult

logger = logging.getLogger(__name__)


class RetrieverJsonCallbackHandler(BaseCallbackHandler):
    """Callback Handler that reorganize logs to json data."""

    def __init__(self, color: Optional[str] = None) -> None:
        """Initialize callback handler."""
        self.logger = logger
        self.color = color

        self.records: Dict[str, Any] = {
            # "on_llm_start_records": [],
            # "on_llm_token_records": [],
            # "on_llm_end_records": [],
            'on_chain_start_records': [],
            'on_chain_end_records': [],
            # "on_tool_start_records": [],
            # "on_tool_end_records": [],
            'on_text_records': [],
            # "on_agent_finish_records": [],
            # "on_agent_action_records": [],
            'action_records': [],
        }

    def on_llm_start(
        self, serialized: Dict[str, Any], prompts: List[str], **kwargs: Any
    ) -> None:
        """Do nothing."""
        pass

    def on_llm_end(self, response: LLMResult, **kwargs: Any) -> None:
        """Do nothing."""
        pass

    def on_llm_new_token(self, token: str, **kwargs: Any) -> None:
        """Do nothing."""
        pass

    def on_llm_error(
        self, error: Union[Exception, KeyboardInterrupt], **kwargs: Any
    ) -> None:
        """Do nothing."""
        pass

    def on_chain_start(
        self, serialized: Dict[str, Any], inputs: Dict[str, Any], **kwargs: Any
    ) -> None:
        """Print out that we are entering a chain."""

        # filter to gest only input documents
        if 'input_documents' in inputs:
            docs = inputs['input_documents']
            input_documents = [
                {'page_content': doc.page_content, 'metadata': doc.metadata}
                for doc in docs
            ]
            json_data = {
                'event_name': 'on_chain_start',
                'inputs': {
                    'input_documents': input_documents,
                    'question': inputs['question'],
                    'chat_history': inputs['chat_history'],
                },
            }
            if json_data not in self.records['on_chain_start_records']:
                self.records['on_chain_start_records'].append(json_data)
            if json_data not in self.records['action_records']:
                self.records['action_records'].append(json_data)

    def on_chain_end(self, outputs: Dict[str, Any], **kwargs: Any) -> None:
        """Print out that we finished a chain."""
        # reponse FAQ
        if 'text' in outputs:
            json_data = {'event_name': 'on_chain_end', 'output': outputs['text']}
            if json_data not in self.records['on_chain_end_records']:
                self.records['on_chain_end_records'].append(json_data)
            if json_data not in self.records['action_records']:
                self.records['action_records'].append(json_data)

    def on_chain_error(
        self, error: Union[Exception, KeyboardInterrupt], **kwargs: Any
    ) -> None:
        """Do nothing."""
        pass

    def on_tool_start(
        self,
        serialized: Dict[str, Any],
        input_str: str,
        **kwargs: Any,
    ) -> None:
        """Do nothing."""
        pass

    def on_agent_action(
        self, action: AgentAction, color: Optional[str] = None, **kwargs: Any
    ) -> Any:
        """Do nothing."""
        pass

    def on_tool_end(
        self,
        output: str,
        color: Optional[str] = None,
        observation_prefix: Optional[str] = None,
        llm_prefix: Optional[str] = None,
        **kwargs: Any,
    ) -> None:
        """Do nothing."""
        pass

    def on_tool_error(
        self, error: Union[Exception, KeyboardInterrupt], **kwargs: Any
    ) -> None:
        """Do nothing."""
        pass

    def on_text(
        self,
        text: str,
        color: Optional[str] = None,
        end: str = '',
        **kwargs: Any,
    ) -> None:
        """Run when agent ends."""
        json_data = {
            'event_name': 'on_text',
            'text': self.remove_on_text_after_prompt(text),
        }
        if json_data not in self.records['on_text_records']:
            self.records['on_text_records'].append(json_data)
        if json_data not in self.records['action_records']:
            self.records['action_records'].append(json_data)

    def on_agent_finish(
        self, finish: AgentFinish, color: Optional[str] = None, **kwargs: Any
    ) -> None:
        """Do nothing."""
        pass

    def show_records(self, record_name: str = None):
        """Show registered records from handler"""
        if record_name != None and record_name in self.records:
            records = self.records[record_name]
        else:
            records = self.records
        return records

    def remove_on_text_after_prompt(self, prompt: str):
        """remove on after prompt and color on prompt"""
        _prefix_colored_text = f'\u001b[32m\033[1;3m'
        _suffix_colored_text = f'\u001b[0m'
        _prompt_after_formatting = 'Prompt after formatting:\n'

        prompt = (
            prompt.replace(_prefix_colored_text, '')
            .replace(_suffix_colored_text, '')
            .replace(_prompt_after_formatting, '')
        )
        return prompt
