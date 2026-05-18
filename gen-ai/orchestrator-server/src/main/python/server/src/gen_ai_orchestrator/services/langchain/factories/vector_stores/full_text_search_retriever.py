import logging
from abc import ABC, abstractmethod

from langchain_core.retrievers import BaseRetriever
from pydantic import ConfigDict

logger = logging.getLogger(__name__)


class FullTextSearchRetriever(BaseRetriever, ABC):
    model_config = ConfigDict(arbitrary_types_allowed=True)

    @abstractmethod
    def prepare_query(self, keywords: list[str]) -> str:
        pass
