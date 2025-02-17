import { emSetting, llmSetting, PromptDefinition } from '../../../shared/model/ai-settings';

export interface RagSettings {
  id: string;
  namespace: string;
  botId: string;
  enabled: boolean;

  debugEnabled: boolean;

  noAnswerSentence: string;
  noAnswerStoryId: string | null;

  questionCondensingLlmSetting: llmSetting;
  questionCondensingPrompt: PromptDefinition;
  maxMessagesFromHistory: number;

  questionAnsweringLlmSetting: llmSetting;
  questionAnsweringPrompt: PromptDefinition;

  emSetting: emSetting;

  indexSessionId: string;
  indexName: string;

  maxDocumentsRetrieved: number;

  documentsRequired: boolean;
}
