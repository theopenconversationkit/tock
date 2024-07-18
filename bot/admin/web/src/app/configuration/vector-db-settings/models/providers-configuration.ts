export enum VectorDbProvider {
  OpenSearch = 'OpenSearch'
}

export interface ProvidersConfigurationParam {
  label: string;
  key: string;
  type: 'text' | 'prompt' | 'list' | 'openlist' | 'number' | 'obfuscated';
  source?: string[];
  inputScale?: 'default' | 'fullwidth';
  defaultValue?: string;
  information?: string;
  min?: number;
  max?: number;
  step?: number;
}

export interface ProvidersConfiguration {
  label: string;
  key: VectorDbProvider;
  params: ProvidersConfigurationParam[];
}

export const ProvidersConfigurations: ProvidersConfiguration[] = [
  {
    label: 'OpenSearch',
    key: VectorDbProvider.OpenSearch,
    params: [
      { key: 'host', label: 'Host', type: 'text' },
      { key: 'port', label: 'Port', type: 'number', min: 1, max: 65535, step: 1 },
      { key: 'username', label: 'User name', type: 'obfuscated' },
      { key: 'password', label: 'Password', type: 'obfuscated' },
      { key: 'indexName', label: 'Index name', type: 'text' },
      {
        key: 'k',
        label: 'k-nearest neighbors',
        type: 'number',
        min: 1,
        max: 100,
        step: 1,
        information: 'Maximum number of nearest neighbors to return in search queries'
      }
    ]
  }
];
