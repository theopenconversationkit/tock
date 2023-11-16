export enum LLMProvider {
  OpenAI = 'OpenAI',
  AzureOpenAIService = 'AzureOpenAIService'
}

export interface RagSettings {
  id: string;
  namespace: string;
  botId: string;
  enabled: boolean;

  noAnswerSentence: string;
  noAnswerStoryId: string;

  llmSetting: llmSetting;
  emSetting: emSetting;
}

export interface llmSetting {
  provider: LLMProvider;

  apiKey: String;
  model: String;

  deploymentName?: String;
  apiBase?: String;
  apiVersion?: String;

  temperature?: Number;
  prompt?: String;
}

export interface emSetting {
  provider: LLMProvider;

  apiKey: String;
  model: String;

  deploymentName?: String;
  apiBase?: String;
  apiVersion?: String;
}
