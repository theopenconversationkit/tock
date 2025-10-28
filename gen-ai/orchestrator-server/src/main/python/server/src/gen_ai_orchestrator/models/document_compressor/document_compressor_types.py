#   Copyright (C) 2024-2025 Credit Mutuel Arkea
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
from typing import Annotated, Union

from fastapi import Body

from gen_ai_orchestrator.models.document_compressor.bloomz.bloomz_compressor_setting import (
    BloomzCompressorSetting,
)

DocumentCompressorSetting = Annotated[
    Union[BloomzCompressorSetting], Body(discriminator='provider')
]
