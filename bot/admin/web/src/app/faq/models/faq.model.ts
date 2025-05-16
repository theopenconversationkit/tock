/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { I18nLabel } from '../../bot/model/i18n';
import { Footnote } from '../../shared/model/dialog-data';

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
  footnotes?: Footnote[];
}
