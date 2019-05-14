package fr.vsct.tock.bot.engine.event

import fr.vsct.tock.bot.engine.user.PlayerId

class ContinuePublicConversationInPrivateEvent(userId: PlayerId,
                                               recipientId: PlayerId,
                                               applicationId: String) : OneToOneEvent(userId, recipientId, applicationId)