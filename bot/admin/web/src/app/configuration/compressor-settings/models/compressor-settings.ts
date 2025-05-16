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

import { CompressorProvider } from './providers-configuration';

export interface CompressorSetting {
  provider: CompressorProvider;

  minScore: number; //(0-1 slider ? score à 0 c'est rien de filtré)
  maxDocuments: number; //(1 min, attention c'est dep du nb de doc remonté par la base vectoriel)
  label: string; // 'entailment'
  endpoint: string; //'https://*********'
}

export interface CompressorSettings {
  id: string;
  namespace: string;
  botId: string;
  enabled: boolean;

  setting: CompressorSetting;
}
