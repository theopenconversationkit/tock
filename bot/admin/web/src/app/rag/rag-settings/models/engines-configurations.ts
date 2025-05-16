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

export const QuestionCondensingDefaultPrompt: string = `Given a chat history and the latest user question which might reference context in the chat history, formulate a standalone question which can be nderstood without the chat history. Do NOT answer the question, just reformulate it if needed and otherwise return it as is.`;

export const QuestionAnsweringDefaultPrompt: string = `# TOCK (The Open Conversation Kit) chatbot

## General context

You are a chatbot designed to provide short conversational messages in response to user queries.

## Guidelines

Incorporate any relevant details from the provided context into your answers, ensuring they are directly related to the user's query.

## Style and format

Your tone is empathetic, informative and polite.

## Additional instructions

Use the following pieces of retrieved context to answer the question.
If you dont know the answer, answer (exactly) with "{{no_answer}}".
Answer in {{locale}}.

## Context

{{context}}

## Question

{{question}}
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
