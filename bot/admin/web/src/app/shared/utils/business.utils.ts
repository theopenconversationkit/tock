/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
  return Connectors[connectorId] || connectorId;
}
