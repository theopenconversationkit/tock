import { emSetting, llmSetting, promptDefinition } from '../../../shared/model/ai-settings';

export interface RagSettings {
  id: string;
  namespace: string;
  botId: string;
  enabled: boolean;

  noAnswerSentence: string;
  noAnswerStoryId: string | null;

  condenseQuestionLlmSetting: llmSetting;
  condenseQuestionPrompt: promptDefinition;

  questionAnsweringLlmSetting: llmSetting;
  questionAnsweringPrompt: promptDefinition;

  emSetting: emSetting;

  indexSessionId: string;
  indexName: string;

  documentsRequired: boolean;
}
