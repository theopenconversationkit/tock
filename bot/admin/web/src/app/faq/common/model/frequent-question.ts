/**
 * Placeholder Type until we really have QA in tock
 */

export enum QaStatus {
  model, deleted
}

export type FrequentQuestion = {
  title: string,
  utterances: string[],
  answer: string,
  enabled: boolean,
  status: QaStatus
};
