import { ObservabilityProvider } from './providers-configuration';

export interface ObservabilitySetting {
  provider: ObservabilityProvider;

  secretKey?: String;
  publicKey?: String;

  url?: String;
}

export interface ObservabilitySettings {
  id: string;
  namespace: string;
  botId: string;
  enabled: boolean;

  setting: ObservabilitySetting;
}
