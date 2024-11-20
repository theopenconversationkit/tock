import { StoryDefinitionConfiguration } from '../../bot/model/story';
import { GenericObject } from './typescript.utils';

export function getStoryIcon(story: StoryDefinitionConfiguration): string {
  if (story.isBuiltIn()) {
    return 'cube';
  }
  if (story.isSimpleAnswer()) {
    return 'chat-left';
  }
  if (story.isScriptAnswer()) {
    return 'code';
  }
}

export const Connectors: GenericObject<string> = {
  web: 'Web',
  google_chat: 'Google Chat',
  messenger: 'Facebook Messenger',
  teams: 'Microsoft Teams',
  whatsapp: 'WhatsApp',
  slack: 'Slack',
  twitter: 'Twitter',
  iadvize: 'iAdvize',
  rocket: 'Rocket.Chat',
  whatsapp_cloud: 'WhatsApp Cloud',
  alexa: 'Amazon Alexa',
  ga: 'Google Assistant',
  businesschat: 'Apple Business Chat'
};

export function getConnectorLabel(connectorId: string): string {
  if (!connectorId) connectorId = 'web';
  return Connectors[connectorId];
}
