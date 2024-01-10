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
from langchain.chains import ConversationalRetrievalChain
from langchain.memory import ChatMessageHistory
from langchain_core.prompts import PromptTemplate

from llm_orchestrator.errors.handlers.openai.openai_exception_handler import (
    openai_exception_handler,
)
from llm_orchestrator.errors.handlers.opensearch.opensearch_exception_handler import (
    opensearch_exception_handler,
)
from llm_orchestrator.models.rag.rag_models import (
    ChatMessageType,
    Footnote,
    TextWithFootnotes,
)
from llm_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)
from llm_orchestrator.routers.requests.requests import RagQuery
from llm_orchestrator.routers.responses.responses import RagResponse
from llm_orchestrator.services.langchain.callbacks.retriever_json_callback_handler import (
    RetrieverJsonCallbackHandler,
)
from llm_orchestrator.services.langchain.factories.langchain_factory import (
    get_em_factory,
    get_llm_factory,
    get_vector_store_factory,
)


@opensearch_exception_handler
@openai_exception_handler(provider='OpenAI or AzureOpenAIService')
def execute_qa_chain(query: RagQuery, debug: bool) -> RagResponse:
    llm_factory = get_llm_factory(setting=query.question_answering_llm_setting)
    em_factory = get_em_factory(setting=query.embedding_question_em_setting)
    vector_store_factory = get_vector_store_factory(
        vector_store_provider=VectorStoreProvider.OPEN_SEARCH,
        embedding_function=em_factory.get_embedding_model(),
        index_name=query.index_name,
    )

    chat = ConversationalRetrievalChain.from_llm(
        llm=llm_factory.get_language_model(),
        retriever=vector_store_factory.get_vector_store().as_retriever(
            search_kwargs=query.document_search_params.to_dict()
        ),
        return_source_documents=True,
        return_generated_question=True,
        combine_docs_chain_kwargs={
            'prompt': PromptTemplate(
                template=llm_factory.setting.prompt,
                input_variables=['context', 'question'],
            )
        },
    )

    message_history = ChatMessageHistory()
    for msg in query.history:
        if ChatMessageType.HUMAN == msg.type:
            message_history.add_user_message(msg.text)
        else:
            message_history.add_ai_message(msg.text)

    records_callback_handler = RetrieverJsonCallbackHandler()

    response = chat(
        inputs={
            'question': query.question,
            'chat_history': message_history.messages,
        },
        callbacks=[records_callback_handler] if debug else [],
    )

    return RagResponse(
        answer=TextWithFootnotes(
            text=response['answer'],
            footnotes=set(
                map(
                    lambda doc: Footnote(
                        identifier=f'{doc.metadata["id"]}',
                        title=doc.metadata['title'],
                        url=doc.metadata['url'],
                    ),
                    response['source_documents'],
                )
            ),
        ),
        debug=records_callback_handler.show_records() if debug else None,
    )
