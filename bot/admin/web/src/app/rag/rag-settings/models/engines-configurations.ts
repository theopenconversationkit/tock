import {
  AzureOpenAiApiVersionsList,
  EnginesConfiguration,
  LLMProvider,
  OllamaEmModelsList,
  OllamaLlmModelsList,
  OpenAIEmbeddingModel,
  OpenAIModelsList
} from '../../../shared/model/ai-settings';

export const DefaultPrompt: string = `# TOCK (The Open Conversation Kit) chatbot

## General context

You are a chatbot designed to provide short conversational messages in response to user queries.

## Guidelines

Incorporate any relevant details from the provided context into your answers, ensuring they are directly related to the user's query.

## Style and format

Your tone is empathetic, informative and polite.

## Additional instructions

Use the following pieces of retrieved context to answer the question.
If you dont know the answer, answer (exactly) with "{no_answer}".
Answer in {locale}.

## Context

{context}

## Question

{question}
`;

const EnginesConfigurations_Llm: EnginesConfiguration[] = [
  {
    label: 'OpenAi',
    key: LLMProvider.OpenAI,
    params: [
      { key: 'apiKey', label: 'Api key', type: 'obfuscated', confirmExport: true },
      { key: 'model', label: 'Model name', type: 'openlist', source: OpenAIModelsList },
      { key: 'temperature', label: 'Temperature', type: 'number', inputScale: 'fullwidth' },
      { key: 'prompt', label: 'Prompt', type: 'prompt', inputScale: 'fullwidth', defaultValue: DefaultPrompt }
    ]
  },
  {
    label: 'Azure OpenAi',
    key: LLMProvider.AzureOpenAIService,
    params: [
      { key: 'apiKey', label: 'Api key', type: 'obfuscated', confirmExport: true },
      { key: 'apiVersion', label: 'Api version', type: 'openlist', source: AzureOpenAiApiVersionsList },
      { key: 'deploymentName', label: 'Deployment name', type: 'text' },
      { key: 'apiBase', label: 'Private endpoint base url', type: 'obfuscated' },
      { key: 'temperature', label: 'Temperature', type: 'number', inputScale: 'fullwidth' },
      { key: 'prompt', label: 'Prompt', type: 'prompt', inputScale: 'fullwidth', defaultValue: DefaultPrompt }
    ]
  },
  {
    label: 'Ollama',
    key: LLMProvider.Ollama,
    params: [
      { key: 'baseUrl', label: 'BaseUrl', type: 'text', defaultValue: 'http://localhost:11434' },
      { key: 'model', label: 'Model', type: 'openlist', source: OllamaLlmModelsList, defaultValue: 'llama2' },
      { key: 'temperature', label: 'Temperature', type: 'number', inputScale: 'fullwidth', defaultValue: 0.7 },
      { key: 'prompt', label: 'Prompt', type: 'prompt', inputScale: 'fullwidth', defaultValue: DefaultPrompt }
    ]
  }
];

const EnginesConfigurations_Embedding: EnginesConfiguration[] = [
  {
    label: 'OpenAi',
    key: LLMProvider.OpenAI,
    params: [
      { key: 'apiKey', label: 'Api key', type: 'obfuscated', confirmExport: true },
      { key: 'model', label: 'Model name', type: 'openlist', source: OpenAIEmbeddingModel }
    ]
  },
  {
    label: 'Azure OpenAi',
    key: LLMProvider.AzureOpenAIService,
    params: [
      { key: 'apiKey', label: 'Api key', type: 'obfuscated', confirmExport: true },
      { key: 'apiVersion', label: 'Api version', type: 'openlist', source: AzureOpenAiApiVersionsList },
      { key: 'deploymentName', label: 'Deployment name', type: 'text' },
      { key: 'apiBase', label: 'Private endpoint base url', type: 'obfuscated' }
    ]
  },
  {
    label: 'Ollama',
    key: LLMProvider.Ollama,
    params: [
      { key: 'baseUrl', label: 'BaseUrl', type: 'text', defaultValue: 'http://localhost:11434' },
      { key: 'model', label: 'Model', type: 'openlist', source: OllamaEmModelsList, defaultValue: 'all-minilm' }
    ]
  }
];

export const EnginesConfigurations: { [key: string]: EnginesConfiguration[] } = {
  llmSetting: EnginesConfigurations_Llm,
  emSetting: EnginesConfigurations_Embedding
};
