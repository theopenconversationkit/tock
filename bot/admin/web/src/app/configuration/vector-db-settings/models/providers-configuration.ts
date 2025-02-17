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
