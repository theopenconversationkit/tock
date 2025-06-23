#   Copyright (C) 2025 Credit Mutuel Arkea
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
from datetime import timedelta
from enum import Enum, auto, unique
from typing import Optional

from pydantic import BaseModel, Field


class FileTemplate(BaseModel):
    name: str = Field(description='The file name.')
    type: str = Field(description='The dataset template.')
    file: str = Field(description='The file containing dataset items.')


class BotInfo(BaseModel):
    namespace: str = Field(description='The namespace.')
    bot_id: str = Field(description='The bot id.')
    file_location: Optional[str] = Field(
        description='Folder where the output will be saved,'
        'stored under $file_location/$NS-$BOTID',
        default=None,
    )


class FromJsonMixin(BaseModel):
    @classmethod
    def from_json_file(cls, file_path: str):
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
            return cls(**data)
        except FileNotFoundError:
            raise ValueError(f"The file '{file_path}' is not found!")
        except json.JSONDecodeError:
            raise ValueError(f"The file '{file_path}' is not a valid JSON!")


@unique
class ActivityStatus(str, Enum):
    COMPLETED = auto()
    STARTED = auto()
    STOPPED = auto()
    FAILED = auto()
    ABANDONED = auto()


class StatusWithReason(BaseModel):
    status: ActivityStatus = Field(description='The activity status.')
    status_reason: str = Field(description='The status reason.', default='')


class ActivityOutput(BaseModel):
    status: StatusWithReason = Field(description='The activity status.')
    duration: timedelta = Field(description='The evaluation time.')
    items_count: int = Field(description='Number of processed items.')
    success_rate: float = Field(description='The success rate.')


class DatasetTemplate(BaseModel):
    type: str = Field(description='The dataset template.')
    file: str = Field(description='The file containing dataset items.')
