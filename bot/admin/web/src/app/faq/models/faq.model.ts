import { I18nLabel } from '../../bot/model/i18n';

export type Utterance = string;

export interface FaqDefinition {
  id?: string;
  intentId?: string;
  intentName?: string;
  language: string;
  applicationName: string;
  creationDate?: Date;
  updateDate?: Date;
  title: string;
  description?: string;
  utterances: Utterance[];
  tags: string[];
  answer: I18nLabel;
  enabled: boolean;
}
