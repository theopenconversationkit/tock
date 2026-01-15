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
"""ProxyServerType Enumeration."""

from enum import Enum, unique


@unique
class ProxyServerType(str, Enum):
    """Enumeration to list Proxy Server types

    This AWSLambda proxy is used when the architecture implemented for the Langfuse
    observability tool places it behind an API Gateway which requires its
    own authentication, itself invoked by an AWS Lambda.
    The API Gateway uses the standard "Authorization" header,
    and uses observability_proxy_server_authorization_header_name
    to define the "Authorization bearer token" for Langfuse.
    """

    AWS_LAMBDA = 'AwsLambda'
