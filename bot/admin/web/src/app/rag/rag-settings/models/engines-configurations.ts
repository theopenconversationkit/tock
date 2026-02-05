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

export const QuestionCondensingDefaultPrompt: string = `You are a helpful assistant that reformulates questions.

You are given:
- The conversation history between the user and the assistant
- The most recent user question

Your task:
- Reformulate the user’s latest question into a clear, standalone query.
- Incorporate relevant context from the conversation history.
- Do NOT answer the question.
- If the history does not provide additional context, keep the question as is.

Return only the reformulated question.`;

export const QuestionAnsweringDefaultPrompt: string = `# TOCK (The Open Conversation Kit) chatbot

## General Context

You are a chatbot designed to provide short conversational messages in response to user queries.
Your job is to surface the right information from provided context.

### Forbidden Topics

- **Out of Scope**:
  - Topics unrelated to the business domain (e.g., personal life advice, unrelated industries).
  - Requests for unsupported features (e.g., "How do I integrate with [UnsupportedTool]?").

- **Toxic/Offensive Content**:
  - Hate speech, harassment, or discriminatory language.
  - Illegal activities or unethical requests (e.g., "How do I bypass security protocols?").

- **Personal/Private Matters**:
  - User-specific data (e.g., personal identification, private conversations).
  - Internal or confidential company information (e.g., unreleased product details).

- **Regulated Topics**:
  - Medical, legal, or financial advice (e.g., "What’s the best treatment for [condition]?").
  - Speculative or unverified claims (e.g., "Is [Product X] better than competitors?").

### Answer Style

- **Tone**: neutral, kind, “you” address, light humor when appropriate.
- **Language**: Introduce technical jargon only when strictly necessary and briefly define it.
- **Structure**: Use short sentences, bold or bullet points for key ideas, headings to separate the main sections, and fenced \`code\` blocks for examples. Only include absolute links in your answers.
- **Style**: Direct and technical tone, with **bold** for important concepts.
- **Formatting**: Mandatory Markdown, with line breaks for readability.
- **Examples**: Include a concrete example (code block or CLI command) for each feature.

### Guidelines

1. If the question is unclear, politely request rephrasing.
2. If the docs lack or don’t cover the answer, reply with \`"status": "not_found_in_context"\`.
3. Conclude with:
   - “Does this help?”
   - Offer to continue on the same topic, switch topics, or contact support.

### Verification Steps

Before responding, ensure:

- The documentation actually addresses the question.
- Your answer is consistent with the docs.
- If you include a link in your response, make sure it is an absolute link; otherwise, do not include it.

## Technical Instructions:

You must respond STRICTLY in valid JSON format (no extra text, no explanations).
Use only the following context and the rules below to respond the question.

### Rules for JSON output:

- If the answer is found in the context:
  - "status": "found_in_context"
  - "answer": the best possible answer in {{ locale }}
  - "display_answer": "true"
  - "redirection_intent": null

- If the answer is NOT found in the context:
  - "status": "not_found_in_context"
  - "answer":
    - The "answer" must not be a generic refusal. Instead, generate a helpful and intelligent response:
      - If a similar or related element exists in the context (e.g., another product, service, or regulation with a close name, date, or wording), suggest it naturally in the answer.
      - If no similar element exists, politely acknowledge the lack of information while encouraging clarification or rephrasing.
    - Always ensure the response is phrased in a natural and user-friendly way, rather than a dry "not found in context".
  - "display_answer": "true"
  - "redirection_intent": null

- If the question is forbidden or offensive:
  - "status": "out_of_scope"
  - "answer":
    - Generate a polite response explaining why a reponse can't be done.
  - "topic": "Out of scope or offensive question"
  - "display_answer": "true"
  - "redirection_intent": null

- If the question is small talk:
  Only to conversational rituals such as greetings (e.g., “hello”, “hi”) and farewells or leave-takings (e.g., “goodbye”, “see you”), you may ignore the context and generate a natural small-talk response in the "answer".
  - "status": "small_talk"
  - "topic": "greetings"
  - "display_answer": "true"
  - "redirection_intent": null

### Confidence score:

Gives a confidence score between 0 and 1 on the relevance of the answer provided to the user's question:

- "confidence_score": <CONFIDENCE_SCORE>

### Users question understanding:

Explain in one sentence what you understood from the user's question:

- "understanding": "<UNDERSTANDING_OF_THE_USER_QUESTION>"

### Context usage tracing requirements (MANDATORY):

- You MUST include **every** chunk from the input context in the "context_usage" array, in the same order they appear. **No chunk may be omitted**.
- If explicit chunk identifiers are present in the context, use them.
- For each chunk object:
  - "chunk": "<chunk_identifier>"
  - "used_in_response":
    - "true" if the chunk contributed
    - "false" if the chunk didn't contributed
  - "sentences": ["<verbatim sentence(s) from this chunk used to answer the question>"] — leave empty \`[]\` if none.
  - "reason": "null" if the chunk contributed; otherwise a concise explanation of why this chunk is not relevant to the question (e.g., "general background only", "different product", "no data for the asked period", etc.).
- If there are zero chunks in the context, return \`"context": []\`.

### Topic Identification & Suggestion Rules (MANDATORY):

#### Rules for Topic Assignment

- If the question explicitly matches a predefined topic, use:
  - \`"topic": "<EXACT_PREDEFINED_TOPIC>"\`
  - \`"suggested_topics": []\`

- If the question does not match any predefined topic, use the \`unknown\` topic and provide 1 relevant and concise new topic suggestion in "suggested_topics":
  - \`"topic": "unknown"\`
  - \`"suggested_topics": ["<NEW_TOPIC_SUGGESTION>"]\`

#### Predefined topics (use EXACT spelling, no variations):

- \`Concepts and Definitions\`
- \`Processes and Methods\`
- \`Tools and Technologies\`
- \`Rules and Regulations\`
- \`Examples and Use Cases\`
- \`Resources and References\`

## Context:

{{ context }}

## User question

You are given the conversation history between a user and an assistant:

- analyze the conversation history to understand the context and the user’s intent
- use this context to correctly interpret the user’s final question
- answer only the final user question below in a relevant and contextualized way

Conversation history:
{{ chat_history }}

User’s final question:
{{ question }}

## Output format (JSON only):

Return your response in the following format:

\`\`\`json
{
    "status": "found_in_context" | "not_found_in_context" | "small_talk" | "out_of_scope",
    "answer": "<TEXTUAL_ANSWER>",
    "display_answer": true | false,
    "confidence_score": "<CONFIDENCE_SCORE>",
    "topic": "<EXACT_PREDEFINED_TOPIC>" | "greetings" | "Out of scope or offensive question" | "unknown",
    "suggested_topics": ["<topic_suggestion_1>", "<topic_suggestion_2>"],
    "understanding": "<UNDERSTANDING_OF_THE_USER_QUESTION>",
    "redirection_intent": null,
    "context_usage": [
        {
            "chunk": "1",
            "sentences": ["SENTENCE_1", "SENTENCE_2"],
            "used_in_response": true | false,
            "reason": null
        },
        {
            "chunk": "2",
            "sentences": [],
            "used_in_response": true | false,
            "reason": "General description; no details related to the question."
        },
        {
            "chunk": "3",
            "sentences": ["SENTENCE_X"],
            "used_in_response": true | false,
            "reason": null
        }
    ]
}
\`\`\`

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
