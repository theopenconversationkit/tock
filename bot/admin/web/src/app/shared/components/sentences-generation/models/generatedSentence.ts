export interface GeneratedSentence {
  sentence: string;
  selected: boolean;
  distinct: boolean;
  errorMessage?: string;
}

export type GeneratedSentenceError = {
  sentence: string;
  message: string;
};
