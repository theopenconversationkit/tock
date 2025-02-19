import json
from enum import Enum, unique, auto
from datetime import timedelta

from pydantic import BaseModel, Field

class BotInfo(BaseModel):
    namespace: str = Field(description='The namespace.')
    bot_id: str = Field(description='The bot id.')
    file_location: str = Field(description='The file location.')

class FromJsonMixin(BaseModel):
    @classmethod
    def from_json_file(cls, file_path: str):
        try:
            with open(file_path, "r", encoding="utf-8") as f:
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
    status_reason: str = Field(description='The status reason.', default="")

class ActivityOutput(BaseModel):
    status: StatusWithReason = Field(description='The activity status.')
    duration: timedelta = Field(description='The evaluation time.')
    nb_dataset_items: int = Field(description='Number of processed items in the dataset.')
    success_rate: float = Field(description='The success rate.')

class DatasetTemplate(BaseModel):
    type: str = Field(description='The dataset template.')
    file: str = Field(description='The file containing dataset items.')
