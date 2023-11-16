import { LLMProvider } from './rag-settings';

export const DefaultPrompt: string = `Use the following context to answer the question at the end.
If you dont know the answer, just say {no_answer}.

Context:
{context}

Question:
{question}

Answer in {locale}:`;

export interface EnginesConfiguration {
  label: string;
  key: LLMProvider;
  params: EnginesConfigurationParam[];
}

export interface EnginesConfigurationParam {
  label: string;
  key: string;
  type: 'text' | 'prompt' | 'list' | 'number';
  source?: string[];
  inputScale?: 'default' | 'fullwidth';
  defaultValue?: string;
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
  'gpt-4',
  'gpt-4-0314',
  'gpt-4-0613',
  'gpt-4-32k',
  'gpt-4-32k-0314',
  'gpt-4-32k-0613',

  'gpt-3.5-turbo',
  'gpt-3.5-turbo-0613',
  'gpt-3.5-turbo-16k',
  'gpt-3.5-turbo-16k-0613',
  'gpt-3.5-turbo-instruct',

  'babbage-002',
  'davinci-002'
];

export const OpenAIEmbeddingModel: string[] = ['text-embedding-ada-002'];

const EnginesConfigurations_Llm: EnginesConfiguration[] = [
  {
    label: 'OpenAi',
    key: LLMProvider.OpenAI,
    params: [
      { key: 'model', label: 'Model name', type: 'list', source: OpenAIModelsList },
      { key: 'apiKey', label: 'Api key', type: 'text' },
      { key: 'temperature', label: 'Temperature', type: 'number', inputScale: 'fullwidth' },
      { key: 'prompt', label: 'Prompt', type: 'prompt', inputScale: 'fullwidth', defaultValue: DefaultPrompt }
    ]
  },
  {
    label: 'Azure OpenAi',
    key: LLMProvider.AzureOpenAIService,
    params: [
      { key: 'model', label: 'Model name', type: 'list', source: OpenAIModelsList },
      { key: 'apiKey', label: 'Api key', type: 'text' },
      { key: 'apiVersion', label: 'Api version', type: 'list', source: AzureOpenAiApiVersionsList },
      { key: 'deploymentName', label: 'Deployment name', type: 'text' },
      { key: 'apiBase', label: 'Private endpoint base url', type: 'text' },
      { key: 'temperature', label: 'Temperature', type: 'number', inputScale: 'fullwidth' },
      { key: 'prompt', label: 'Prompt', type: 'prompt', inputScale: 'fullwidth', defaultValue: DefaultPrompt }
    ]
  }
];

const EnginesConfigurations_Embedding: EnginesConfiguration[] = [
  {
    label: 'OpenAi',
    key: LLMProvider.OpenAI,
    params: [
      { key: 'model', label: 'Model name', type: 'list', source: OpenAIEmbeddingModel },
      { key: 'apiKey', label: 'Api key', type: 'text' }
    ]
  },
  {
    label: 'Azure OpenAi',
    key: LLMProvider.AzureOpenAIService,
    params: [
      { key: 'model', label: 'Model name', type: 'list', source: OpenAIEmbeddingModel },
      { key: 'apiKey', label: 'Api key', type: 'text' },
      { key: 'apiVersion', label: 'Api version', type: 'list', source: AzureOpenAiApiVersionsList },
      { key: 'deploymentName', label: 'Deployment name', type: 'text' },
      { key: 'apiBase', label: 'Private endpoint base url', type: 'text' }
    ]
  }
];

export const EnginesConfigurations: { [key: string]: EnginesConfiguration[] } = {
  llmSetting: EnginesConfigurations_Llm,
  emSetting: EnginesConfigurations_Embedding
};
