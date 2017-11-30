package fr.vsct.tock.bot.connector.slack.model


data class AttachmentField(val title: String,
                           val value: String,
                           val short: Boolean = false)