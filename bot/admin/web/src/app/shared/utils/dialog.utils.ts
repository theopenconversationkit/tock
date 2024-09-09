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
