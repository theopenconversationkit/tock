package fr.vsct.tock.bot.connector.slack.model


data class SlackMessageAttachment(val fields: List<AttachmentField>,
                                   val fallback: String,
                                   val color: String,
                                   val text: String? = null,
                                   val pretext: String? = null): SlackConnectorMessage()