import {
  AzureOpenAiApiVersionsList,
  AiEngineSettingKeyName,
  EnginesConfiguration,
  AiEngineProvider,
  OllamaEmModelsList,
  OllamaLlmModelsList,
  OpenAIEmbeddingModel,
  OpenAIModelsList,
  ProvidersConfigurationParam
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
    source: ['jinja2', 'f-string'],
    defaultValue: 'jinja2',
    inputScale: 'fullwidth'
  },
  {
    key: 'template',
    label: 'Prompt template',
    type: 'prompt',
    inputScale: 'fullwidth',
    defaultValue: QuestionCondensingDefaultPrompt,
    information: 'LangChain ChatPromptTemplate'
  }
];

export const QuestionAnswering_prompt: ProvidersConfigurationParam[] = [
  {
    key: 'formatter',
    label: 'Prompt template format',
    type: 'radio',
    source: ['jinja2', 'f-string'],
    defaultValue: 'jinja2',
    inputScale: 'fullwidth'
  },
  {
    key: 'template',
    label: 'Prompt template',
    type: 'prompt',
    inputScale: 'fullwidth',
    defaultValue: QuestionAnsweringDefaultPrompt
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
      // { key: 'prompt', label: 'Prompt', type: 'prompt', inputScale: 'fullwidth', defaultValue: DefaultPrompt }
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
      // { key: 'prompt', label: 'Prompt', type: 'prompt', inputScale: 'fullwidth', defaultValue: DefaultPrompt }
    ]
  },
  {
    label: 'Ollama',
    key: AiEngineProvider.Ollama,
    params: [
      { key: 'baseUrl', label: 'BaseUrl', type: 'text', defaultValue: 'http://localhost:11434' },
      { key: 'model', label: 'Model', type: 'openlist', source: OllamaLlmModelsList, defaultValue: 'llama2' },
      { key: 'temperature', label: 'Temperature', type: 'number', inputScale: 'fullwidth', min: 0, max: 1, step: 0.05, defaultValue: 0.7 }
      // { key: 'prompt', label: 'Prompt', type: 'prompt', inputScale: 'fullwidth', defaultValue: DefaultPrompt }
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
    | AiEngineSettingKeyName.condenseQuestionLlmSetting
    | AiEngineSettingKeyName.emSetting
  >]: EnginesConfiguration[];
} = {
  condenseQuestionLlmSetting: EnginesConfigurations_Llm,
  questionAnsweringLlmSetting: EnginesConfigurations_Llm,
  emSetting: EnginesConfigurations_Embedding
};
