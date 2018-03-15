package fr.vsct.tock.bot.connector.messenger.model.send

import com.fasterxml.jackson.annotation.JsonProperty

data class CustomEvent(@JsonProperty("_eventName") val eventName: String)
