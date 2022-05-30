export type Utterance = string;

export interface FaqDefinition {
  id?: string;
  intentId?: string;
  intentName?: string;
  language: string;
  applicationId: string;
  creationDate?: Date;
  updateDate?: Date;
  title: string;
  description?: string;
  utterances: Utterance[];
  tags: string[];
  answer: string;
  enabled: boolean;
}
export interface FaqFilter {
  enabled: boolean;
  search: string;
  tags: Array<string>;
  sort;
}

export interface FaqTrainingFilter {
  search: string;
}
