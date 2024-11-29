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
