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
from langchain_core.documents import Document

from gen_ai_orchestrator.services.langchain.callbacks.retriever_json_callback_handler import (
    RetrieverJsonCallbackHandler,
)


def test_retriever_json_callback_handler_on_chain_start():
    """Check records are added (in the correct entries)"""
    handler = RetrieverJsonCallbackHandler()
    _inputs = {
        'input_documents': [
            Document(
                page_content='some page content',
                metadata={'some meta': 'some meta value'},
            )
        ],
        'question': 'What is happening?',
        'chat_history': [],
    }
    handler.on_chain_start(serialized={}, inputs=_inputs)
    expected_json_data = {
        'event_name': 'on_chain_start',
        'inputs': {
            'input_documents': [
                {
                    'page_content': 'some page content',
                    'metadata': {'some meta': 'some meta value'},
                }
            ],
            'question': _inputs['question'],
            'chat_history': _inputs['chat_history'],
        },
    }
    assert handler.records['on_chain_start_records'][0] == expected_json_data
    assert handler.records['action_records'][0] == expected_json_data


def test_retriever_json_callback_handler_on_chain_start_no_double_entries():
    """Check records are added only once in history."""
    handler = RetrieverJsonCallbackHandler()
    _inputs = {
        'input_documents': [
            Document(
                page_content='some page content',
                metadata={'some meta': 'some meta value'},
            )
        ],
        'question': 'What is happening?',
        'chat_history': [],
    }
    handler.on_chain_start(serialized={}, inputs=_inputs)
    expected_json_data = {
        'event_name': 'on_chain_start',
        'inputs': {
            'input_documents': [
                {
                    'page_content': 'some page content',
                    'metadata': {'some meta': 'some meta value'},
                }
            ],
            'question': _inputs['question'],
            'chat_history': _inputs['chat_history'],
        },
    }
    assert expected_json_data in handler.records['on_chain_start_records']
    assert expected_json_data in handler.records['action_records']
    assert len(handler.records['on_chain_start_records']) == 1
    assert len(handler.records['action_records']) == 1
    handler.on_chain_start(serialized={}, inputs=_inputs)
    assert expected_json_data in handler.records['on_chain_start_records']
    assert expected_json_data in handler.records['action_records']
    assert len(handler.records['on_chain_start_records']) == 1
    assert len(handler.records['action_records']) == 1


def test_retriever_json_callback_handler_on_chain_start_no_inputs():
    """Check no records are added if none are present in chain inputs."""
    handler = RetrieverJsonCallbackHandler()
    _inputs = {'question': 'What is happening?', 'chat_history': []}
    handler.on_chain_start(serialized={}, inputs=_inputs)
    assert len(handler.records['on_chain_start_records']) == 0
    assert len(handler.records['action_records']) == 0


def test_retriever_json_callback_handler_on_chain_end():
    """Check records are added (in the correct entries)"""
    handler = RetrieverJsonCallbackHandler()
    _outputs = {
        'text': 'This is what is happening',
    }
    handler.on_chain_end(outputs=_outputs)
    expected_json_data = {
        'event_name': 'on_chain_end',
        'output': 'This is what is happening',
    }
    assert handler.records['on_chain_end_records'][0] == expected_json_data
    assert handler.records['action_records'][0] == expected_json_data


def test_retriever_json_callback_handler_on_text():
    """Check records are added (in the correct entries)"""
    handler = RetrieverJsonCallbackHandler()
    handler.on_text(text='Some text arrives')
    expected_json_data = {
        'event_name': 'on_text',
        'text': 'Some text arrives',
    }
    assert handler.records['on_text_records'][0] == expected_json_data
    assert handler.records['action_records'][0] == expected_json_data
