import { ProvidersConfigurationParam } from '../../../shared/model/ai-settings';

export enum ObservabilityProvider {
  Langfuse = 'Langfuse'
}

export interface ObservabilityProvidersConfiguration {
  label: string;
  key: ObservabilityProvider;
  params: ProvidersConfigurationParam[];
}

export const ProvidersConfigurations: ObservabilityProvidersConfiguration[] = [
  {
    label: 'Langfuse',
    key: ObservabilityProvider.Langfuse,
    params: [
      { key: 'publicKey', label: 'Public key', type: 'obfuscated' },
      { key: 'secretKey', label: 'Secret key', type: 'obfuscated', confirmExport: true },
      { key: 'url', label: 'Url', type: 'obfuscated' },
      { key: 'publicUrl', label: 'Public url', type: 'obfuscated', required: false }
    ]
  }
];
