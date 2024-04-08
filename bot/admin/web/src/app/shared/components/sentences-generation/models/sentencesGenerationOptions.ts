export interface SentencesGenerationOptions {
  spellingMistakes: boolean;
  smsLanguage: boolean;
  abbreviatedLanguage: boolean;
  llmTemperature: number;
  sentencesExample: string[];
}
