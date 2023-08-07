export interface LlmEngineConfigurationParams {
  label: string;
  key: string;
  type: 'string' | 'list';
  source?: string[];
}

export interface LlmEngineConfiguration {
  label: string;
  key: string;
  params: LlmEngineConfigurationParams[];
}
