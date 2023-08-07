import { LlmEngineConfiguration } from '.';

export const AzureOpenAiApiVersions = ['2023-03-15-preview', '2022-12-01', '2023-05-15', '2023-06-01-preview'];

export const OpenAiModels = ['gpt-4', 'gpt-4-32k'];

export const EmbeddingEngines = [
  'text-embedding-ada-002',
  'text-search-davinci-*-001',
  'text-search-curie-*-001',
  'text-search-babbage-*-001',
  'text-search-ada-*-001'
];

export const LlmEngines: LlmEngineConfiguration[] = [
  {
    label: 'OpenAi',
    key: 'openAi',
    params: [
      { key: 'apiKey', label: 'Api key', type: 'string' },
      { key: 'modelName', label: 'Model name', type: 'list', source: OpenAiModels }
    ]
  },
  {
    label: 'Azure OpenAi',
    key: 'azureOpenAi',
    params: [
      { key: 'modelName', label: 'Model name', type: 'list', source: OpenAiModels },
      { key: 'deploymentName', label: 'Deployment name', type: 'string' },
      { key: 'privateEndpointBaseUrl', label: 'Private endpoint base url', type: 'string' },
      { key: 'apiVersion', label: 'Api version', type: 'list', source: AzureOpenAiApiVersions }
    ]
  }
];

export const DefaultPrompt = `Use the following context to answer the question at the end.
If you dont know the answer, just say {no_answer}.

Context:
{context}

Question:
{question}

Answer in {locale}:`;
