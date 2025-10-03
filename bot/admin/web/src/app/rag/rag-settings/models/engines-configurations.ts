/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {
  AzureOpenAiApiVersionsList,
  AiEngineSettingKeyName,
  EnginesConfiguration,
  AiEngineProvider,
  OllamaEmModelsList,
  OllamaLlmModelsList,
  OpenAIEmbeddingModel,
  OpenAIModelsList,
  ProvidersConfigurationParam,
  PromptDefinitionFormatter
} from '../../../shared/model/ai-settings';

export const QuestionCondensingDefaultPrompt: string = `# Question Reformulation Assistant

## Context
You are a helpful assistant that reformulates questions.

You are given:
- The conversation history between the user and the assistant
- The most recent user question

## Task
1. Reformulate the user’s latest question into a clear, standalone query.
2. Incorporate relevant context from the conversation history.
3. Enrich the reformulation with the business/domain lexicon whenever relevant.
4. Expand any acronym into its full meaning, and also keep the acronym in parentheses.
 - Example: "PTZ" → "Prêt à Taux Zéro (PTZ)"
5. If the user provides the full term without acronym, add the acronym in parentheses if it is commonly used in the business domain.
 - Example: "Prêt à Taux Zéro" → "Prêt à Taux Zéro (PTZ)"
6. Do NOT answer the question.

## Business/domain lexicon
PTZ : Prêt à Taux Zéro
Éco-PTZ : Éco-Prêt à Taux Zéro

## Output
Return only the reformulated question.
`;

export const QuestionAnsweringDefaultPrompt: string = `# TOCK (The Open Conversation Kit) chatbot

## Instructions:
You must answer STRICTLY in valid JSON format (no extra text, no explanations).
Use only the following context and the rules below to answer the question.

### Rules for JSON output:

- If the answer is found in the context:
  - "status": "found_in_context"

- If the answer is NOT found in the context:
  - "status": "not_found_in_context"
  - "answer":
    - The "answer" must not be a generic refusal. Instead, generate a helpful and intelligent response:
        - If a similar or related element exists in the context (e.g., another product, service, or regulation with a close name, date, or wording), suggest it naturally in the answer.
        - If no similar element exists, politely acknowledge the lack of information while encouraging clarification or rephrasing.
    - Always ensure the response is phrased in a natural and user-friendly way, rather than a dry "not found in context".

- If the question matches a special case defined below:
  - "status": "<the corresponding case code>"

And for all cases (MANDATORY):
  - "answer": "<the best possible answer in {{ locale }}>"
  - "topic": "<exactly ONE topic chosen STRICTLY from the predefined list below. If no exact match is possible, set 'unknown'>"
  - "suggested_topics": ["<zero or more free-form suggestions if topic is unknown>"]

Exception: If the question is small talk (only to conversational rituals such as greetings (e.g., “hello”, “hi”) and farewells or leave-takings (e.g., “goodbye”, “see you”) ), you may ignore the context and generate a natural small-talk response in the "answer". In this case:
  - "status": "small_talk"
  - "topic": "<e.g., greetings>"
  - "suggested_topics": []
  - "context": []

### Context tracing requirements (MANDATORY):
- You MUST include **every** chunk from the input context in the "context" array, in the same order they appear. **No chunk may be omitted**.
- If explicit chunk identifiers are present in the context, use them; otherwise assign sequential numbers starting at 1.
- For each chunk object:
  - "chunk": "<chunk_identifier_or_sequential_number>"
  - "sentences": ["<verbatim sentence(s) from this chunk used to answer the question>"] — leave empty \`[]\` if none.
  - "reason": null if the chunk contributed; otherwise a concise explanation of why this chunk is not relevant to the question (e.g., "general background only", "different product", "no data for the asked period", etc.).
- If there are zero chunks in the context, return \`"context": []\`.

### Predefined list of topics (use EXACT spelling, no variations):

## Context:
{{ context }}

## Conversation history
{{ chat_history }}

## User question
{{ question }}

## Output format (JSON only):
Return your response in the following format:

{
  "status": "found_on_context" | "not_in_context" | "small_talk",
  "answer": "TEXTUAL_ANSWER",
  "topic": "EXACT_TOPIC_FROM_LIST_OR_UNKNOWN",
  "suggested_topics": [
    "SUGGESTED_TOPIC_1",
    "SUGGESTED_TOPIC_2"
  ],
  "context": [
    {
      "chunk": "1",
      "sentences": ["SENTENCE_1", "SENTENCE_2"],
      "reason": null
    },
    {
      "chunk": "2",
      "sentences": [],
      "reason": "General description; no details related to the question."
    },
    {
      "chunk": "3",
      "sentences": ["SENTENCE_X"],
      "reason": null
    }
  ]
}
`;

export const QuestionCondensing_prompt: ProvidersConfigurationParam[] = [
  {
    key: 'formatter',
    label: 'Prompt template format',
    type: 'radio',
    source: [PromptDefinitionFormatter.jinja2, PromptDefinitionFormatter.fstring],
    defaultValue: PromptDefinitionFormatter.jinja2,
    inputScale: 'fullwidth'
  },
  {
    key: 'template',
    label: 'Prompt template',
    type: 'prompt',
    inputScale: 'fullwidth',
    defaultValue: QuestionCondensingDefaultPrompt,
    information: "See LangChain's ChatPromptTemplate for more infos"
  }
];

export const QuestionAnswering_prompt: ProvidersConfigurationParam[] = [
  {
    key: 'formatter',
    label: 'Prompt template format',
    type: 'radio',
    source: [PromptDefinitionFormatter.jinja2, PromptDefinitionFormatter.fstring],
    defaultValue: PromptDefinitionFormatter.jinja2,
    inputScale: 'fullwidth'
  },
  {
    key: 'template',
    label: 'Prompt template',
    type: 'prompt',
    inputScale: 'fullwidth',
    defaultValue: QuestionAnsweringDefaultPrompt,
    rows: 16
  }
];

const EnginesConfigurations_Llm: EnginesConfiguration[] = [
  {
    label: 'OpenAI',
    key: AiEngineProvider.OpenAI,
    params: [
      { key: 'apiKey', label: 'Api key', type: 'obfuscated', confirmExport: true },
      { key: 'baseUrl', label: 'Base url', type: 'text', defaultValue: 'https://api.openai.com/v1' },
      { key: 'model', label: 'Model name', type: 'openlist', source: OpenAIModelsList },
      { key: 'temperature', label: 'Temperature', type: 'number', inputScale: 'fullwidth', min: 0, max: 1, step: 0.05 }
    ]
  },
  {
    label: 'Azure OpenAI',
    key: AiEngineProvider.AzureOpenAIService,
    params: [
      { key: 'apiKey', label: 'Api key', type: 'obfuscated', confirmExport: true },
      { key: 'apiVersion', label: 'Api version', type: 'openlist', source: AzureOpenAiApiVersionsList },
      { key: 'deploymentName', label: 'Deployment name', type: 'text' },
      { key: 'model', label: 'Model name', type: 'openlist', source: OpenAIModelsList },
      { key: 'apiBase', label: 'Base url', type: 'obfuscated' },
      { key: 'temperature', label: 'Temperature', type: 'number', inputScale: 'fullwidth', min: 0, max: 1, step: 0.05 }
    ]
  },
  {
    label: 'Ollama',
    key: AiEngineProvider.Ollama,
    params: [
      { key: 'baseUrl', label: 'BaseUrl', type: 'text', defaultValue: 'http://localhost:11434' },
      { key: 'model', label: 'Model', type: 'openlist', source: OllamaLlmModelsList, defaultValue: 'llama2' },
      { key: 'temperature', label: 'Temperature', type: 'number', inputScale: 'fullwidth', min: 0, max: 1, step: 0.05, defaultValue: 0.7 }
    ]
  }
];

const EnginesConfigurations_Embedding: EnginesConfiguration[] = [
  {
    label: 'OpenAI',
    key: AiEngineProvider.OpenAI,
    params: [
      { key: 'apiKey', label: 'Api key', type: 'obfuscated', confirmExport: true },
      { key: 'baseUrl', label: 'Base url', type: 'text', defaultValue: 'https://api.openai.com/v1' },
      { key: 'model', label: 'Model name', type: 'openlist', source: OpenAIEmbeddingModel }
    ]
  },
  {
    label: 'Azure OpenAI',
    key: AiEngineProvider.AzureOpenAIService,
    params: [
      { key: 'apiKey', label: 'Api key', type: 'obfuscated', confirmExport: true },
      { key: 'apiVersion', label: 'Api version', type: 'openlist', source: AzureOpenAiApiVersionsList },
      { key: 'deploymentName', label: 'Deployment name', type: 'text' },
      { key: 'model', label: 'Model name', type: 'openlist', source: OpenAIEmbeddingModel },
      { key: 'apiBase', label: 'Base url', type: 'obfuscated' }
    ]
  },
  {
    label: 'Ollama',
    key: AiEngineProvider.Ollama,
    params: [
      { key: 'baseUrl', label: 'BaseUrl', type: 'text', defaultValue: 'http://localhost:11434' },
      { key: 'model', label: 'Model', type: 'openlist', source: OllamaEmModelsList, defaultValue: 'all-minilm' }
    ]
  }
];

export const EnginesConfigurations: {
  [K in Extract<
    AiEngineSettingKeyName,
    | AiEngineSettingKeyName.questionAnsweringLlmSetting
    | AiEngineSettingKeyName.questionCondensingLlmSetting
    | AiEngineSettingKeyName.emSetting
  >]: EnginesConfiguration[];
} = {
  questionCondensingLlmSetting: EnginesConfigurations_Llm,
  questionAnsweringLlmSetting: EnginesConfigurations_Llm,
  emSetting: EnginesConfigurations_Embedding
};
