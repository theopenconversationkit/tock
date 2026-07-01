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
"""VectorStoreProvider Enumeration."""

from enum import Enum, unique


@unique
class VectorStoreProvider(str, Enum):
    """Enumeration to list Vector Store Provider types"""

    OPEN_SEARCH = 'OpenSearch'
    PGVECTOR = 'PGVector'

    @classmethod
    def has_value(cls, value):
        return value in cls._value2member_map_
