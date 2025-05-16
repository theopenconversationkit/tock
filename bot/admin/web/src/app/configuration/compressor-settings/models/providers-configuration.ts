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

import { ProvidersConfigurationParam } from '../../../shared/model/ai-settings';

export enum CompressorProvider {
  BloomzRerank = 'BloomzRerank'
}

export interface CompressorProvidersConfiguration {
  label: string;
  key: CompressorProvider;
  params: ProvidersConfigurationParam[];
}

export const ProvidersConfigurations: CompressorProvidersConfiguration[] = [
  {
    label: 'BloomzRerank',
    key: CompressorProvider.BloomzRerank,
    params: [
      {
        key: 'label',
        label: 'Label',
        type: 'text',
        information: 'Name of the positive scoring output label (see models card for more info)'
      },
      { key: 'endpoint', label: 'Endpoint', type: 'obfuscated' },
      {
        key: 'minScore',
        label: 'Minimum score',
        type: 'number',
        min: 0,
        max: 1,
        step: 0.05,
        information: 'Score below which documents will not be used to generate the answer'
      },
      {
        key: 'maxDocuments',
        label: 'Max documents',
        type: 'number',
        min: 1,
        max: 20,
        step: 1,
        information: 'Maximum number of documents to be proposed as sources for the answer'
      }
    ]
  }
];
