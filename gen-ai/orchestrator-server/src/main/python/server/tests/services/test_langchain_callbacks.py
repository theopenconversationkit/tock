#   Copyright (C) 2024-2026 Credit Mutuel Arkea
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
from langchain_core.messages import AIMessage, HumanMessage, SystemMessage
from langchain_core.prompt_values import ChatPromptValue, StringPromptValue

from gen_ai_orchestrator.services.langchain.callbacks.rag_callback_handler import (
    RAGCallbackHandler,
)


def test_rag_callback_handler_qa_documents():
    """Check records are added (in the correct entries)"""
    handler = RAGCallbackHandler()
    docs = [Document(
        page_content='some page content',
        metadata={'some meta': 'some meta value'},
    )]
    handler.on_chain_start(serialized={},
                           inputs={'documents': docs},
                           **{'name': 'RunnableAssign<answer>'})
    assert handler.records['documents'] == docs

def test_rag_callback_handler_chat_prompt_output():
    """Check records are added (in the correct entries)"""
    handler = RAGCallbackHandler()
    llm_output = 'llm result !'
    handler.on_chain_start(serialized={},
                           inputs=AIMessage(content=llm_output),
                           **{'name': 'chat_chain_output'})
    assert handler.records['chat_chain_output'] == llm_output

def test_rag_callback_handler_qa_prompt_output():
    """Check records are added (in the correct entries)"""
    handler = RAGCallbackHandler()
    llm_output = 'llm result !'
    handler.on_chain_start(serialized={},
                           inputs=AIMessage(content=llm_output),
                           **{'name': 'rag_chain_output'})
    assert handler.records['rag_chain_output'] == llm_output

def test_rag_callback_handler_chat_prompt():
    """Check records are added (in the correct entries)"""
    handler = RAGCallbackHandler()
    prompt = 'A custom prompt !'
    outputs = ChatPromptValue(messages=[
        SystemMessage(content=prompt),
        HumanMessage(content='hi !')
    ])
    handler.on_chain_end(serialized={}, outputs=outputs)
    assert handler.records['chat_prompt'] == prompt

def test_rag_callback_handler_qa_prompt():
    """Check records are added (in the correct entries)"""
    handler = RAGCallbackHandler()
    prompt = 'A custom prompt !'
    handler.on_chain_end(serialized={}, outputs=StringPromptValue(text=prompt))
    assert handler.records['rag_prompt'] == prompt
