export enum VectorDbProvider {
  OpenSearch = 'OpenSearch'
}

export interface ProvidersConfigurationParam {
  label: string;
  key: string;
  type: 'text' | 'prompt' | 'list' | 'openlist' | 'number' | 'obfuscated';
  source?: string[];
  inputScale?: 'default' | 'fullwidth';
  defaultValue?: string | number;
  computedDefaultValue?: (parametres: any) => string;
  information?: string;
  min?: number;
  max?: number;
  step?: number;
  readonly?: boolean;
  disabled?: boolean;
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
      { key: 'host', label: 'Host', type: 'text', defaultValue: 'localhost' },
      { key: 'port', label: 'Port', type: 'number', min: 1, max: 65535, step: 1, defaultValue: '9200' },
      { key: 'username', label: 'User name', type: 'obfuscated', defaultValue: 'admin' },
      { key: 'password', label: 'Password', type: 'obfuscated', defaultValue: 'admin' },
      {
        key: 'k',
        label: 'k-nearest neighbors',
        type: 'number',
        min: 1,
        max: 100,
        step: 1,
        defaultValue: 4,
        information: 'Maximum number of nearest neighbors to return in search queries'
      },
      {
        key: 'vectorSize',
        label: 'Vector size',
        type: 'number',
        min: 1,
        step: 1,
        defaultValue: 1536
      }
    ]
  }
];
