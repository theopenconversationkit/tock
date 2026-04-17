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

export const dialogMessageUserIdentities = {
  client: { qualifier: 'Human', avatar: 'assets/images/scenario-client.svg' },
  bot: { qualifier: 'Bot', avatar: 'assets/images/scenario-bot.svg' }
};

export function getDialogMessageUserQualifier(isBot: boolean): string {
  if (isBot) return dialogMessageUserIdentities.bot.qualifier;
  return dialogMessageUserIdentities.client.qualifier;
}

export function getDialogMessageUserAvatar(isBot: boolean): string {
  if (isBot) return dialogMessageUserIdentities.bot.avatar;
  return dialogMessageUserIdentities.client.avatar;
}

export enum RagAnswerStatus {
  FOUND_IN_CONTEXT = 'found_in_context',
  NOT_FOUND_IN_CONTEXT = 'not_found_in_context',
  SMALL_TALK = 'small_talk',
  OUT_OF_SCOPE = 'out_of_scope',
  HUMAN_ESCALATION = 'human_escalation',
  INJECTION_ATTEMPT = 'injection_attempt',
  TECHNICAL_ERROR = 'technical_error'
}

export const RagAnswerStatusLabels: Record<RagAnswerStatus, string> = {
  [RagAnswerStatus.FOUND_IN_CONTEXT]: 'Found in context',
  [RagAnswerStatus.NOT_FOUND_IN_CONTEXT]: 'Not found in context',
  [RagAnswerStatus.SMALL_TALK]: 'Small talk',
  [RagAnswerStatus.HUMAN_ESCALATION]: 'Human escalation',
  [RagAnswerStatus.OUT_OF_SCOPE]: 'Out of scope',
  [RagAnswerStatus.INJECTION_ATTEMPT]: 'Injection attempt',
  [RagAnswerStatus.TECHNICAL_ERROR]: 'Technical error'
};

export const RagAnswerStatusDisplayOrder = [
  RagAnswerStatus.FOUND_IN_CONTEXT,
  RagAnswerStatus.NOT_FOUND_IN_CONTEXT,
  RagAnswerStatus.SMALL_TALK,
  RagAnswerStatus.HUMAN_ESCALATION,
  RagAnswerStatus.OUT_OF_SCOPE,
  RagAnswerStatus.INJECTION_ATTEMPT,
  RagAnswerStatus.TECHNICAL_ERROR
];

export const RagAnswerStatusIcons: Record<RagAnswerStatus, string> = {
  [RagAnswerStatus.FOUND_IN_CONTEXT]: 'check-circle',
  [RagAnswerStatus.NOT_FOUND_IN_CONTEXT]: 'x-circle',
  [RagAnswerStatus.SMALL_TALK]: 'chat-dots',
  [RagAnswerStatus.OUT_OF_SCOPE]: 'slash-circle',
  [RagAnswerStatus.HUMAN_ESCALATION]: 'person-raised-hand',
  [RagAnswerStatus.INJECTION_ATTEMPT]: 'shield-exclamation',
  [RagAnswerStatus.TECHNICAL_ERROR]: 'bug'
};
