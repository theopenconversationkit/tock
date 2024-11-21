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
      { key: 'label', label: 'Label', type: 'text' },
      { key: 'endpoint', label: 'Endpoint', type: 'obfuscated' },
      { key: 'minScore', label: 'Minimum score', type: 'number', min: 0, max: 1, step: 0.05 },
      { key: 'maxDocuments', label: 'Max documents', type: 'number', min: 1, max: 50, step: 1 }
    ]
  }
];
