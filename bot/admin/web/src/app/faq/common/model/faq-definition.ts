/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

import {Utterance} from "./utterance";

export type FaqDefinitionStatus = 'draft' | 'deleted'; // frontend view status

export type FaqDefinition = {
  id?: string,
  intentId?: string,
  language: string,
  applicationId: string,
  creationDate?: Date,
  updateDate?: Date,
  title: string,
  description?: string,
  utterances: Utterance[],
  tags: string[],
  answer: string,
  status: FaqDefinitionStatus,
  enabled: boolean
};

export function blankFaqDefinition(config: { language: string, applicationId: string }): FaqDefinition {
  return {
    id: undefined,
    intentId: undefined,
    title: '',
    description: '',
    utterances: [],
    tags: [],
    answer: '',
    enabled: true,
    status: 'draft',
    ...config
  };
}
