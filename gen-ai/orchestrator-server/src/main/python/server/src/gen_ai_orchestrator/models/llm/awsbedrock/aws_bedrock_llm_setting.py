#   Copyright (C) 2023-2026 Credit Mutuel Arkea
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
"""Model for creating AwsBedrockLLMSetting."""

from typing import Literal

from pydantic import Field

from gen_ai_orchestrator.models.llm.llm_provider import LLMProvider
from gen_ai_orchestrator.models.llm.llm_setting import BaseLLMSetting


class AwsBedrockLLMSetting(BaseLLMSetting):
    """
    A class for AWS Bedrock Large Language Model Setting.
    Usage docs: https://docs.aws.amazon.com/bedrock/latest/userguide/what-is-bedrock.html

    Authentication relies on the default AWS credential chain (environment variables,
    shared credentials/config file, or an IAM role). The AWS profile name, if any, is
    configured once for the whole orchestrator-server via the
    `tock_gen_ai_orchestrator_aws_bedrock_credentials_profile_name` environment property
    (see `application_settings`), and is not part of this per-request setting.
    If that property is not set, requests fail unless the
    `tock_gen_ai_orchestrator_aws_bedrock_credentials_allow_default_profile` environment
    property is set to `true`, in which case the default AWS credential chain (e.g. IRSA,
    instance profile, environment variables) is used instead.
    The AWS region is likewise not configured here: it is resolved from the AWS
    profile/credential chain (e.g. the `region` set in the AWS config file for the
    selected profile), consistently with the profile name resolution.
    No explicit access/secret keys are stored.
    """

    provider: Literal[LLMProvider.AWS_BEDROCK] = Field(
        description='The Large Language Model Provider.',
        examples=[LLMProvider.AWS_BEDROCK],
    )
    model: str = Field(
        description='The Bedrock model id',
        examples=['anthropic.claude-3-5-sonnet-20240620-v1:0'],
        min_length=1,
    )
