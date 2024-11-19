export enum ObservabilityProvider {
  Langfuse = 'Langfuse'
}

export interface ProvidersConfigurationParam {
  label: string;
  key: string;
  type: 'text' | 'prompt' | 'list' | 'openlist' | 'number' | 'obfuscated';
  source?: string[];
  inputScale?: 'default' | 'fullwidth';
  defaultValue?: string;
  confirmExport?: boolean;
}

export interface ProvidersConfiguration {
  label: string;
  key: ObservabilityProvider;
  params: ProvidersConfigurationParam[];
}

export const ProvidersConfigurations: ProvidersConfiguration[] = [
  {
    label: 'Langfuse',
    key: ObservabilityProvider.Langfuse,
    params: [
      { key: 'publicKey', label: 'Public key', type: 'obfuscated' },
      { key: 'secretKey', label: 'Secret key', type: 'obfuscated', confirmExport: true },
      { key: 'url', label: 'Url', type: 'obfuscated' }
    ]
  }
];
