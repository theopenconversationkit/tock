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
      { key: 'label', label: 'Label', type: 'text', information: 'Name of the positive scoring output label' },
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
