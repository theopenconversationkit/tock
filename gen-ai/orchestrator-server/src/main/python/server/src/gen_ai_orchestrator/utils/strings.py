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
"""Utility module"""


def obfuscate(sentence: str):
    """
    Obfuscate methode that hide all character (with "*")
    and only keeps the first 2 char and last 1 if the string is longer than 4 chars.
    For example, it is used to hide passwords in logs
    """

    if len(sentence) < 4:
        return '*****'
    else:
        return sentence[:2] + ('*' * (len(sentence) - 3)) + sentence[-1:]
