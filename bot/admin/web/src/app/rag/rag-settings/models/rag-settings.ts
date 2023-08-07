export interface RagSettings {
  id: string;
  namespace: string;
  botId: string;
  enabled: boolean;
  engine: string;
  temperature: number;
  embeddingEngine: string;
  prompt: string;
  noAnswerSentence: string;
  noAnswerStoryId: string;
  params: RagSettingsParams[];
}

export interface RagSettingsParams {
  apiKey?: string;
  modelName?: string;
  deploymentName?: string;
  privateEndpointBaseUrl?: string;
  apiVersion?: string;

  embeddingDeploymentName?: string;
  embeddingModelName?: string;
  embeddingApiKey?: string;
  embeddingApiVersion?: string;
}
