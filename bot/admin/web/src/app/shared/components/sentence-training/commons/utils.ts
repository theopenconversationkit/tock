import { normalizeString } from '../../../utils';
import { SentenceExtended } from '../sentence-training.component';

export function getSentenceId(sentence: SentenceExtended): string {
  return `stnc_${normalizeString(sentence.text)}`;
}
