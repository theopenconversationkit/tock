package ai.tock.bot.engine.event

import ai.tock.bot.engine.user.PlayerId

class ContinuePublicConversationInPrivateEvent(userId: PlayerId,
                                               recipientId: PlayerId,
                                               applicationId: String) : OneToOneEvent(userId, recipientId, applicationId)