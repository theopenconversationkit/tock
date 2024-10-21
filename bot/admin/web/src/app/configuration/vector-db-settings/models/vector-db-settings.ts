import { VectorDbProvider } from './providers-configuration';

export interface VectorDbSetting {
  provider: VectorDbProvider;

  secretKey?: String;
  publicKey?: String;

  url?: String;
}

export interface VectorDbSettings {
  id: string;
  namespace: string;
  botId: string;
  enabled: boolean;

  setting: VectorDbSetting;
}
