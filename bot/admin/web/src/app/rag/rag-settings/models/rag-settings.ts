import { emSetting, llmSetting } from '../../../shared/model/ai-settings';

export interface RagSettings {
  id: string;
  namespace: string;
  botId: string;
  enabled: boolean;

  noAnswerSentence: string;
  noAnswerStoryId: string | null;

  llmSetting: llmSetting;
  emSetting: emSetting;

  indexSessionId: string;
  indexName: string;

  documentsRequired: boolean;
}
