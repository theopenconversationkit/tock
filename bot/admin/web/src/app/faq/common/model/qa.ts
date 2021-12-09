/**
 * Placeholder Type until we really have QA in tock
 */

export enum QaStatus {
  model, deleted
}

export type Qa = {
  title: string,
  label: string,
  description: string,
  enabled: boolean,
  status: QaStatus
};
