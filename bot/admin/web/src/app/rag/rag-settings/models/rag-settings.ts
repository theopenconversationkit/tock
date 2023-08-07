export interface RagSettingsParams {
  apiKey?: string;
  modelName?: string;
  deploymentName?: string;
  privateEndpointBaseUrl?: string;
  apiVersion?: string;
}

export interface RagSettings {
  _id: string;
  namespace: string;
  botId: string;
  enabled: boolean;
  engine: string;
  temperature: number;
  embeddingEngine: string;
  prompt: string;
  noAnswerRedirection: string;
  params: RagSettingsParams[];
}
