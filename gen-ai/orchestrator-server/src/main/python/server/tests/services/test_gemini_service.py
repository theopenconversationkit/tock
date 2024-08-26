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
from unittest.mock import patch

from fastapi import UploadFile

from gen_ai_orchestrator.services.gemini.gemini_service import send_images


@patch('gen_ai_orchestrator.services.gemini.gemini_service.ChatVertexAI')
@patch('gen_ai_orchestrator.services.gemini.gemini_service.prepare_images_from_pdf')
@patch('gen_ai_orchestrator.services.gemini.gemini_service.prepare_images')
async def test_send_images(mocked_image_list, mocked_pdf_image_list, mocked_llm):
    pdf_file = UploadFile('file1')
    images_list = [UploadFile('file2'), UploadFile('file3')]
    question = 'My question'
    model = 'My model name'
    project_id = 'My project ID'
    location = 'The location I want'
    temperature = 0.7

    mocked_image_list.return_value = []
    mocked_pdf_image_list.return_value = []
    tet = mocked_llm.return_value
    tet.invoke = "Here is the model's response"

    response = await send_images(
        files=pdf_file,
        question=question,
        model=model,
        project_id=project_id,
        location=location,
        temperature=temperature,
    )

    mocked_pdf_image_list.assert_called_once_with(pdf_file)
    mocked_llm.assert_called_once()
    assert response == "Here is the model's response"

    response2 = await send_images(
        files=images_list,
        question=question,
        model=model,
        project_id=project_id,
        location=location,
        temperature=temperature,
    )

    mocked_image_list.assert_called_once_with(images_list)
    assert response2 == "Here is the model's response"
