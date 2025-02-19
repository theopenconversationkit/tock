import os
from datetime import datetime

import humanize
from gen_ai_orchestrator.models.observability.langfuse.langfuse_setting import LangfuseObservabilitySetting
from pydantic import BaseModel, Field

from scripts.common.models import FromJsonMixin, ActivityOutput, BotInfo, DatasetTemplate


class DatasetInfo(BaseModel):
    name: str = Field(description='The dataset name.')
    description: str = Field(description='The dataset description.', default="")
    metadata: dict = Field(description='The dataset metadata.', default={})
    template: DatasetTemplate = Field(description='The dataset template.')

class CreateDatasetInput(FromJsonMixin):
    bot: BotInfo = Field(description='The bot information.')
    dataset: DatasetInfo = Field(description='The dataset information.')
    observability_setting: LangfuseObservabilitySetting = Field(
        description='The Langfuse observability settings.'
    )

    def format(self):
        header_text = " CREATE DATASET INPUT "
        details_str = f"""
        Langfuse environment : {str(self.observability_setting.url)}
        The dataset name     : {self.dataset.name}
        The dataset template : {self.dataset.template.file}
        """

        # Find the longest line in the details
        details = details_str.splitlines()
        max_detail_length = max(len(detail) for detail in details)
        # Construct the header and separator lines
        header_line = header_text.center(max_detail_length, '-')
        separator = '-' * max_detail_length

        to_string = f"{header_line}\n{details_str}\n{separator}"
        return "\n".join(line.strip() for line in to_string.splitlines() if line.strip())


class CreateDatasetOutput(ActivityOutput):
    dataset_name: str = Field(description='The dataset name.')

    def format(self):
        header_text = " CREATE DATASET OUTPUT "
        details_str = f"""
        The dataset name               : {self.dataset_name}
        Number of items in dataset     : {self.nb_dataset_items}
        Rate of successful experiment  : {self.success_rate:.2f}%
        Duration                       : {humanize.precisedelta(self.duration)}
        Date                           : {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}
        """
        status_str = f"""
        Status                         : {self.status.status.name}
        Reason                         : {self.status.status_reason}
        """

        # Find the longest line in the details
        details = details_str.splitlines()
        max_detail_length = max(len(detail) for detail in details)
        # Construct the header and separator lines
        header_line = header_text.center(max_detail_length, '-')
        separator = '-' * max_detail_length

        to_string = f"{header_line}\n{details_str}\n{separator}\n{status_str}\n{separator}"
        return "\n".join(line.strip() for line in to_string.splitlines() if line.strip())
