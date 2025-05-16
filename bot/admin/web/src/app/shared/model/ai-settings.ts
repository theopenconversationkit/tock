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

export interface ProvidersConfigurationParam {
  label: string;
  key: string;
  type: 'text' | 'prompt' | 'list' | 'openlist' | 'number' | 'obfuscated' | 'radio';
  numberControlType?: 'slider' | 'input';
  source?: string[];
  inputScale?: 'default' | 'fullwidth';
  defaultValue?: string | number;
  information?: string;
  min?: number;
  max?: number;
  step?: number;
  rows?: number;
  confirmExport?: boolean;
  required?: boolean;
}

export enum AiEngineProvider {
  OpenAI = 'OpenAI',
  AzureOpenAIService = 'AzureOpenAIService',
  Ollama = 'Ollama'
}

export enum AiEngineSettingKeyName {
  questionCondensingLlmSetting = 'questionCondensingLlmSetting',
  questionAnsweringLlmSetting = 'questionAnsweringLlmSetting',
  llmSetting = 'llmSetting',
  emSetting = 'emSetting'
}

export enum PromptTypeKeyName {
  questionCondensingPrompt = 'questionCondensingPrompt',
  questionAnsweringPrompt = 'questionAnsweringPrompt'
}

export enum PromptDefinitionFormatter {
  jinja2 = 'jinja2',
  fstring = 'f-string'
}

export interface PromptDefinition {
  formatter: PromptDefinitionFormatter;
  template: string;
}

export interface llmSetting {
  provider: AiEngineProvider;

  model: String;

  apiKey?: String;
  deploymentName?: String;
  apiBase?: String;
  apiVersion?: String;

  temperature?: Number;
}

export interface emSetting {
  provider: AiEngineProvider;

  model: String;

  apiKey?: String;
  deploymentName?: String;
  apiBase?: String;
  apiVersion?: String;
}

export interface EnginesConfiguration {
  label: string;
  key: AiEngineProvider;
  params: ProvidersConfigurationParam[];
}

export const AzureOpenAiApiVersionsList: string[] = [
  '2022-12-01',
  '2023-05-15',
  '2023-06-01-preview',
  '2023-07-01-preview',
  '2023-08-01-preview',
  '2023-09-01-preview'
];

export const OpenAIModelsList: string[] = [
  'gpt-4o',
  'gpt-4o-mini',

  'gpt-4',
  'gpt-4-0314',
  'gpt-4-0613',
  'gpt-4-32k',
  'gpt-4-32k-0314',
  'gpt-4-32k-0613',
  'gpt-4-turbo',

  'gpt-3.5-turbo',
  'gpt-3.5-turbo-0613',
  'gpt-3.5-turbo-16k',
  'gpt-3.5-turbo-16k-0613',
  'gpt-3.5-turbo-instruct',

  'babbage-002',
  'davinci-002'
];

export const OpenAIEmbeddingModel: string[] = ['text-embedding-3-small', 'text-embedding-3-large', 'text-embedding-ada-002'];

export const OllamaLlmModelsList: string[] = ['llama2', 'llama3', 'llama3.1', 'llama3.1:8b', 'llama3.2'];

export const OllamaEmModelsList: string[] = ['mxbai-embed-large', 'nomic-embed-text', 'all-minilm'];
