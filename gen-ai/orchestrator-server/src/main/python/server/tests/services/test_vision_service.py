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
import base64
from unittest.mock import AsyncMock, MagicMock, patch

import pytest
from fastapi import UploadFile

from gen_ai_orchestrator.models.llm.vertexai.vertexai_llm_setting import (
    VertexAILLMSetting,
)
from gen_ai_orchestrator.models.observability.langfuse.langfuse_setting import (
    LangfuseObservabilitySetting,
)
from gen_ai_orchestrator.services.vision.vision_service import (
    ask_model_with_files,
    convert_pdf_2_image,
    prepare_images,
)


@pytest.mark.asyncio
@patch('gen_ai_orchestrator.services.vision.vision_service.convert_from_bytes')
async def test_convert_pdf_2_image(mock_convert_from_bytes):
    # Mock the PDF file
    pdf_content = b'%PDF-1.4 sample pdf content'
    mock_file = MagicMock(spec=UploadFile)
    mock_file.read = AsyncMock(return_value=pdf_content)
    mock_file.filename = 'test.pdf'

    # Mock the convert_from_bytes function
    mock_image = MagicMock()
    mock_convert_from_bytes.return_value = [mock_image]

    # Mock image saving
    mock_image.save = lambda img_arr, format: img_arr.write(b'image bytes')

    images = await convert_pdf_2_image(mock_file)

    # Assertions
    assert len(images) == 1
    assert images[0] == b'image bytes'


@pytest.mark.asyncio
async def test_prepare_images_with_png():
    # Mock a PNG file
    png_content = b'png file content'
    mock_file = MagicMock(spec=UploadFile)
    mock_file.read = AsyncMock(return_value=png_content)
    mock_file.filename = 'test.png'

    # Test prepare_images
    images = await prepare_images([mock_file])

    # Assertions
    assert len(images) == 1
    assert images[0] == base64.b64encode(png_content).decode('utf-8')


@pytest.mark.asyncio
@patch('gen_ai_orchestrator.services.vision.vision_service.convert_pdf_2_image')
async def test_prepare_images_with_pdf(mock_convert_pdf_2_image):
    # Mock a PDF file
    mock_pdf_images = [b'image1 bytes', b'image2 bytes']
    mock_file = MagicMock(spec=UploadFile)
    mock_file.filename = 'test.pdf'

    mock_convert_pdf_2_image.return_value = mock_pdf_images

    # Test prepare_images
    images = await prepare_images([mock_file])

    # Assertions
    assert len(images) == 2
    assert images[0] == base64.b64encode(mock_pdf_images[0]).decode('utf-8')
    assert images[1] == base64.b64encode(mock_pdf_images[1]).decode('utf-8')


@pytest.mark.asyncio
@patch('gen_ai_orchestrator.services.vision.vision_service.prepare_images')
@patch(
    'gen_ai_orchestrator.services.langchain.factories.langchain_factory.create_observability_callback_handler'
)
@patch('gen_ai_orchestrator.services.vision.vision_service.get_llm_factory')
async def test_ask_model_with_files(
    mock_get_llm_factory,
    mock_create_observability_callback_handler,
    mock_prepare_images,
):
    # Mock LLMSetting and ObservabilitySetting
    mock_llm_setting = MagicMock(spec=VertexAILLMSetting)
    mock_llm_setting.prompt = 'test prompt'
    mock_observability_setting = MagicMock(spec=LangfuseObservabilitySetting)

    # Mock UploadFile
    mock_file = MagicMock(spec=UploadFile)
    mock_file.filename = 'test.png'
    mock_file.read = AsyncMock(return_value=b'png content')

    # Mock model factory and language model
    mock_model = MagicMock()
    mock_model.invoke = lambda messages, callbacks: MagicMock(content='model response')

    mock_llm_factory = MagicMock()
    mock_llm_factory.get_language_model = lambda: mock_model

    mock_get_llm_factory.return_value = mock_llm_factory
    mock_prepare_images.return_value = ['image_encoded']

    # Test the function
    response = await ask_model_with_files(
        files=[mock_file],
        llm_setting=mock_llm_setting,
        observability_setting=mock_observability_setting,
    )

    mock_create_observability_callback_handler.assert_called_once()

    # Assertions
    assert response.sentences == ['model response']
