export interface CompletionRequest {
  sentences: string[];
  locale: string;
  options: CompletionOptions;
  llmTemperature: number;
}

export interface CompletionOptions {
  abbreviatedLanguage: boolean;
  smsLanguage: boolean;
  spellingMistakes: boolean;
}

export interface CompletionResponse {
  sentences: string[];
}
