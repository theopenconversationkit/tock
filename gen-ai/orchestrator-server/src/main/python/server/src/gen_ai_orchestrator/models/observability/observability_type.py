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
"""Module defining generic type alias"""

from typing import Annotated, Union

from fastapi import Body

from gen_ai_orchestrator.models.observability.langfuse.langfuse_setting import LangfuseObservabilitySetting

ObservabilitySetting = Annotated[
    Union[LangfuseObservabilitySetting],
    Body(discriminator='provider'),
]
