/**
 * Placeholder Type until we really have QA in tock
 */

import {somewhatSimilar, verySimilar } from "../util/string-utils";

export enum QaStatus {
  draft, model, deleted
}

export type Utterance = {
  value: string;
};

export const utteranceEquals = (a: Utterance, b: Utterance) => a.value === b.value;

export const utteranceEquivalent = (a: Utterance, b: Utterance) => verySimilar(a.value, b.value);

export const utteranceSomewhatSimilar = (a: Utterance, b: Utterance) => somewhatSimilar(a.value, b.value);

export type FrequentQuestion = {
  title: string,
  utterances: Utterance[],
  answer: string,
  enabled: boolean,
  status: QaStatus
};

export function blankFrequentQuestion(): FrequentQuestion {
  return {
    title: '',
    utterances: [],
    answer: '',
    enabled: true,
    status: QaStatus.draft
  };
}
