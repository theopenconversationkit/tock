/**
 * Placeholder Type until we really have QA in tock
 */

import {somewhatSimilar, verySimilar } from "../util/string-utils";

export enum QaStatus {
  draft,  inbox, validated, model, deleted
}

export type Utterance = string;

export const utteranceEquals = (a: Utterance, b: Utterance) => a === b;

export const utteranceEquivalent = (a: Utterance, b: Utterance) => verySimilar(a, b);

export const utteranceSomewhatSimilar = (a: Utterance, b: Utterance) => somewhatSimilar(a, b);

export type FrequentQuestion = {
  id?: string,
  language: string,
  applicationName: string,
  creationDate?: Date,
  updateDate?: Date,
  title: string,
  description?: string,
  utterances: Utterance[],
  tags: string[],
  answer: string,
  enabled: boolean,
  status: QaStatus
};

export function blankFrequentQuestion(config: {language: string, applicationName: string}): FrequentQuestion {
  return {
    id: undefined,
    title: '',
    description: '',
    utterances: [],
    tags: [],
    answer: '',
    enabled: true,
    status: QaStatus.inbox,
    ...config
  };
}
