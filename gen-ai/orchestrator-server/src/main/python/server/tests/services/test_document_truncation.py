#   Copyright (C) 2026 Credit Mutuel Arkea
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

from gen_ai_orchestrator.models.rag.rag_models import RAGDocumentMetadata
from gen_ai_orchestrator.services.langchain.rag_chain_builder import (
    TRUNCATION_MARKER,
    format_documents_as_context,
    truncate_oversized_documents,
)

METADATA = {'index_session_id': 's', 'id': 'd1', 'title': 't', 'chunk': '1/1'}


def test_small_document_untouched():
    doc = Document(page_content='a' * 10, metadata=dict(METADATA))
    assert truncate_oversized_documents([doc], 10) == [doc]


def test_oversized_document_truncated_with_metadata():
    doc = Document(page_content='a' * 100, metadata=dict(METADATA))
    [result] = truncate_oversized_documents([doc], 10)
    assert result.metadata['truncated'] is True
    assert result.metadata['original_length'] == 100
    assert result.metadata['truncated_length'] == len(result.page_content)
    assert 'truncated' not in doc.metadata
    meta = RAGDocumentMetadata(**result.metadata)
    assert meta.truncated is True and meta.original_length == 100


def test_marker_appended_and_counted_in_truncated_length():
    doc = Document(page_content='one two three four', metadata=dict(METADATA))
    [result] = truncate_oversized_documents([doc], 6, 100)
    assert result.page_content == 'one two' + TRUNCATION_MARKER.format(
        original_length=18
    )
    assert result.metadata['truncated_length'] == len(result.page_content)


def test_marker_is_sent_to_the_llm():
    doc = Document(page_content='one two three four', metadata=dict(METADATA))
    [result] = truncate_oversized_documents([doc], 6, 100)
    assert 'content truncated: original length 18 characters' in (
        format_documents_as_context([result])
    )


def test_trailing_whitespace_stripped_before_marker():
    doc = Document(page_content='one two three', metadata=dict(METADATA))
    [result] = truncate_oversized_documents([doc], 8, 100)
    assert result.page_content == 'one two' + TRUNCATION_MARKER.format(
        original_length=13
    )


def test_cut_on_word_boundary_not_extended():
    doc = Document(page_content='one two three', metadata=dict(METADATA))
    [result] = truncate_oversized_documents([doc], 7, 100)
    assert result.page_content == 'one two' + TRUNCATION_MARKER.format(
        original_length=13
    )


def test_no_whitespace_completion_is_capped():
    doc = Document(page_content='a' * 1000, metadata=dict(METADATA))
    [result] = truncate_oversized_documents([doc], 10, 100)
    assert result.page_content == 'a' * 110 + TRUNCATION_MARKER.format(
        original_length=1000
    )


def test_word_completion_reaching_end_leaves_document_untouched():
    doc = Document(page_content='a ' * 9999 + 'abcdef', metadata=dict(METADATA))
    [result] = truncate_oversized_documents([doc], 20000, 100)
    assert result is doc
    assert 'truncated' not in result.metadata


def test_disabled_when_non_positive():
    doc = Document(page_content='a' * 100, metadata=dict(METADATA))
    assert truncate_oversized_documents([doc], 0) == [doc]
