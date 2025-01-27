import { PromptDefinition, llmSetting } from '../../../shared/model/ai-settings';

export interface SentenceGenerationSettings {
  id: string;
  namespace: string;
  botId: string;
  enabled: boolean;

  llmSetting: llmSetting;
  prompt: PromptDefinition;
}
