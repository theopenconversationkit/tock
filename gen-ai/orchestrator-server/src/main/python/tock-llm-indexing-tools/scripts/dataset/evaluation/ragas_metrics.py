#   Copyright (C) 2025-2026 Credit Mutuel Arkea
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
from ragas.metrics import (
    BleuScore,
    ExactMatch,
    FactualCorrectness,
    Faithfulness,
    LLMContextPrecisionWithoutReference,
    LLMContextRecall,
    NonLLMStringSimilarity,
    ResponseRelevancy,
    RougeScore,
    SemanticSimilarity,
    StringPresence,
)

ragas_available_metrics = [
    # -------------- Retrieval Augmented Generation ----------------
    # Context Precision is a metric that measures the proportion of relevant chunks in the retrieved_contexts.
    {'name': 'LLMContextPrecisionWithoutReference', 'metric': LLMContextPrecisionWithoutReference(), 'hasReason': True},
    # Context Recall measures how many of the relevant documents (or pieces of information) were successfully retrieved.
    # It focuses on not missing important results.
    # Higher recall means fewer relevant documents were left out. In short, recall is about not missing anything important.
    # Since it is about not missing anything, calculating context recall always requires a reference to compare against.
    {'name': 'LLMContextRecall', 'metric': LLMContextRecall(), 'hasReason': True},
    # The ResponseRelevancy metric measures how relevant a response is to the user input.
    # Higher scores indicate better alignment with the user input, while lower scores are given if the response is incomplete or includes redundant information.
    # This metric is calculated using the user_input and the response
    {'name': 'ResponseRelevancy', 'metric': ResponseRelevancy(), 'hasReason': False},
    # The Faithfulness metric measures how factually consistent a response is with the retrieved context.
    # It ranges from 0 to 1, with higher scores indicating better consistency.
    {'name': 'Faithfulness', 'metric': Faithfulness(), 'hasReason': True},

    # # -------------- Natural Language Comparison ----------------
    # FactualCorrectness is a metric that compares and evaluates the factual accuracy of the generated response with the reference.
    # This metric is used to determine the extent to which the generated response aligns with the reference.
    # The factual correctness score ranges from 0 to 1, with higher values indicating better performance
    {'name': 'FactualCorrectness', 'metric': FactualCorrectness(), 'hasReason': True},
    # The concept of Answer Semantic Similarity pertains to the assessment of the semantic resemblance between the generated answer and the ground truth.
    # This evaluation is based on the ground truth and the answer, with values falling within the range of 0 to 1.
    # A higher score signifies a better alignment between the generated answer and the ground truth.
    {'name': 'SemanticSimilarity', 'metric': SemanticSimilarity(), 'hasReason': False},
    # NonLLMStringSimilarity metric measures the similarity between the reference and the response using traditional string distance measures such as Levenshtein, Hamming, and Jaro.
    # This metric is useful for evaluating the similarity of response to the reference text without relying on large language models (LLMs).
    # The metric returns a score between 0 and 1, where 1 indicates a perfect match between the response and the reference.
    # This is a non LLM based metric.
    {'name': 'NonLLMStringSimilarity', 'metric': NonLLMStringSimilarity(), 'hasReason': False},
    # The BleuScore score is a metric used to evaluate the quality of response by comparing it with reference.
    # It measures the similarity between the response and the reference based on n-gram precision and brevity penalty.
    # BLEU score was originally designed to evaluate machine translation systems, but it is also used in other natural language processing tasks.
    # BLEU score ranges from 0 to 1, where 1 indicates a perfect match between the response and the reference.
    # This is a non LLM based metric.
    {'name': 'BleuScore', 'metric': BleuScore(), 'hasReason': False},
    # The RougeScore score is a set of metrics used to evaluate the quality of natural language generations.
    # It measures the overlap between the generated response and the reference text based on n-gram recall, precision, and F1 score.
    # ROUGE score ranges from 0 to 1, where 1 indicates a perfect match between the response and the reference.
    # This is a non LLM based metric.
    {'name': 'RougeScore', 'metric': RougeScore(), 'hasReason': False},
    # The StringPresence metric checks if the response contains the reference text.
    # It is useful in scenarios where you need to ensure that the generated response contains certain keywords or phrases.
    # The metric returns 1 if the response contains the reference, and 0 otherwise.
    {'name': 'StringPresence', 'metric': StringPresence(), 'hasReason': False},
    # The ExactMatch metric checks if the response is exactly the same as the reference text.
    # It is useful in scenarios where you need to ensure that the generated response matches the expected output word-for-word.
    # For example, arguments in tool calls, etc.
    # The metric returns 1 if the response is an exact match with the reference, and 0 otherwise.
    {'name': 'ExactMatch', 'metric': ExactMatch(), 'hasReason': False},
]