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
from typing import List, Optional
from urllib.parse import urljoin

import requests
from langchain_core.output_parsers.transform import (
    BaseCumulativeTransformOutputParser,
)
from pydantic import BaseModel
from requests.exceptions import HTTPError


class GuardrailOutput(BaseModel):
    content: str
    output_toxicity: bool = False
    output_toxicity_reason: Optional[list[str]] = []


class BloomzGuardrailOutputParser(BaseCumulativeTransformOutputParser[dict]):
    """Parse the output of an LLM call using Guardrails."""

    max_score: float
    """Maximum acceptable toxicity score."""
    endpoint: str
    """The model API endpoint to use."""
    diff: bool = True

    @classmethod
    def is_lc_serializable(cls) -> bool:
        """Return whether this class is serializable."""
        return True

    @classmethod
    def get_lc_namespace(cls) -> List[str]:
        """Get the namespace of the langchain object."""
        return ['langchain', 'schema', 'output_parser']

    @property
    def _type(self) -> str:
        """Return the output parser type for serialization."""
        return 'default'

    def _diff(self, prev: Optional[dict], next: dict) -> dict:
        output = next.copy()
        if prev:
            output['content'] = next['content'][len(prev['content']) :]
        return output

    def parse(self, text: str) -> dict:
        response = requests.post(
            urljoin(self.endpoint, '/guardrail'), json={'text': [text]}
        )
        if response.status_code != 200:
            raise HTTPError(
                f"Error {response.status_code}. Bloomz guardrail didn't respond as expected."
            )

        results = response.json()['response'][0]

        detected_toxicities = list(
            filter(lambda mode: mode['score'] > self.max_score, results)
        )

        return GuardrailOutput(
            content=text,
            output_toxicity=bool(detected_toxicities),
            output_toxicity_reason=list(
                map(lambda mode: mode['label'], detected_toxicities)
            ),
        ).model_dump()
