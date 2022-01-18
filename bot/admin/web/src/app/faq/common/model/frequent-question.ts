/**
 * Placeholder Type until we really have QA in tock
 */

import { SentenceStatus } from "src/app/model/nlp";
import {somewhatSimilar, verySimilar } from "../util/string-utils";

export type Utterance = string;

export const utteranceEquals = (a: Utterance, b: Utterance) => a === b;
export const utteranceEquivalent = (a: Utterance, b: Utterance) => verySimilar(a, b);
export const utteranceSomewhatSimilar = (a: Utterance, b: Utterance) => somewhatSimilar(a, b);

export type FrequentQuestionStatus = 'Draft' | 'Model' | 'Deleted'; // Adapt to your needs

export type FrequentQuestion = {
  id?: string,
  language: string,
  applicationId: string,
  creationDate?: Date,
  updateDate?: Date,
  title: string,
  description?: string,
  utterances: Utterance[],
  tags: string[],
  answer: string,
  status: FrequentQuestionStatus,
  enabled: boolean
};

export function blankFrequentQuestion(config: {language: string, applicationId: string}): FrequentQuestion {
  return {
    id: undefined,
    title: '',
    description: '',
    utterances: [],
    tags: [],
    answer: '',
    enabled: true,
    status: 'Draft',
    ...config
  };
}
