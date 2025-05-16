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

export enum VectorDbProvider {
  OpenSearch = 'OpenSearch',
  PGVector = 'PGVector'
}

export interface VectorDbProvidersConfiguration {
  label: string;
  key: VectorDbProvider;
  params: ProvidersConfigurationParam[];
}

export const ProvidersConfigurations: VectorDbProvidersConfiguration[] = [
  {
    label: 'OpenSearch',
    key: VectorDbProvider.OpenSearch,
    params: [
      { key: 'host', label: 'Host', type: 'text', defaultValue: 'localhost' },
      { key: 'port', label: 'Port', type: 'number', numberControlType: 'input', min: 1, max: 65535, step: 1, defaultValue: '9200' },
      { key: 'username', label: 'User name', type: 'obfuscated', defaultValue: 'admin' },
      { key: 'password', label: 'Password', type: 'obfuscated', defaultValue: 'admin', confirmExport: true }
    ]
  },
  {
    label: 'PGVector',
    key: VectorDbProvider.PGVector,
    params: [
      { key: 'host', label: 'Host', type: 'text', defaultValue: 'localhost' },
      { key: 'port', label: 'Port', type: 'number', numberControlType: 'input', min: 1, max: 65535, step: 1, defaultValue: '5432' },
      { key: 'username', label: 'User name', type: 'obfuscated', defaultValue: 'postgres' },
      { key: 'password', label: 'Password', type: 'obfuscated', defaultValue: 'ChangeMe', confirmExport: true },
      {
        key: 'database',
        label: 'Database',
        type: 'text',
        defaultValue: 'postgres'
      }
    ]
  }
];
