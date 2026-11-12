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
import json
from unittest.mock import patch

import pytest

from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIConnectionErrorException,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo
from gen_ai_orchestrator.services.rag import rag_service


@pytest.mark.asyncio
async def test_rag_stream_emits_error_event_on_business_exception():
    async def failing(*_):
        yield {'event': 'token', 'data': '{}'}
        raise GenAIConnectionErrorException(
            ErrorInfo(error='APIConnectionError', cause='Connection error.')
        )

    with patch.object(rag_service, 'execute_rag_chain_stream', failing):
        events = [e async for e in rag_service.rag_stream(None, False)]

    assert [e['event'] for e in events] == ['token', 'error']
    payload = json.loads(events[-1]['data'])
    assert payload['code'] == 1001
    assert payload['info']['cause'] == 'Connection error.'


@pytest.mark.asyncio
async def test_rag_stream_emits_error_event_on_unexpected_exception():
    async def failing(*_):
        raise ValueError('boom')
        yield

    with patch.object(rag_service, 'execute_rag_chain_stream', failing):
        events = [e async for e in rag_service.rag_stream(None, False)]

    assert events[-1]['event'] == 'error'
    assert json.loads(events[-1]['data'])['info']['cause'] == 'boom'
