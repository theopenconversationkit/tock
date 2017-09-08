/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.ga

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.ga.model.GAIntent
import fr.vsct.tock.bot.connector.ga.model.request.GAPermission
import fr.vsct.tock.bot.connector.ga.model.response.GABasicCard
import fr.vsct.tock.bot.connector.ga.model.response.GAButton
import fr.vsct.tock.bot.connector.ga.model.response.GACarouselSelect
import fr.vsct.tock.bot.connector.ga.model.response.GAExpectedInput
import fr.vsct.tock.bot.connector.ga.model.response.GAExpectedIntent
import fr.vsct.tock.bot.connector.ga.model.response.GAImage
import fr.vsct.tock.bot.connector.ga.model.response.GAInputPrompt
import fr.vsct.tock.bot.connector.ga.model.response.GAItem
import fr.vsct.tock.bot.connector.ga.model.response.GALinkOutSuggestion
import fr.vsct.tock.bot.connector.ga.model.response.GAListItem
import fr.vsct.tock.bot.connector.ga.model.response.GAListSelect
import fr.vsct.tock.bot.connector.ga.model.response.GAOptionInfo
import fr.vsct.tock.bot.connector.ga.model.response.GAOptionValueSpec
import fr.vsct.tock.bot.connector.ga.model.response.GAPermissionValueSpec
import fr.vsct.tock.bot.connector.ga.model.response.GARichResponse
import fr.vsct.tock.bot.connector.ga.model.response.GASimpleResponse
import fr.vsct.tock.bot.connector.ga.model.response.GASimpleSelect
import fr.vsct.tock.bot.connector.ga.model.response.GAStructuredResponse
import fr.vsct.tock.bot.connector.ga.model.response.GASuggestion
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.Parameters
import fr.vsct.tock.bot.definition.StoryStep
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.translator.TextAndVoiceTranslatedString
import fr.vsct.tock.translator.UserInterfaceType.textAndVoiceAssistant
import fr.vsct.tock.translator.UserInterfaceType.textChat
import fr.vsct.tock.translator.isSSML
import mu.KotlinLogging

/**
 *
 */
private val logger = KotlinLogging.logger {}

val gaConnectorType = ConnectorType("ga", textAndVoiceAssistant, false)

fun BotBus.withGoogleAssistant(messageProvider: () -> ConnectorMessage): BotBus {
    return with(gaConnectorType, messageProvider)
}

fun BotBus.withGoogleVoiceAssistant(messageProvider: () -> ConnectorMessage): BotBus {
    if (userInterfaceType != textChat) {
        with(gaConnectorType, messageProvider)
    }
    return this
}

fun BotBus.permissionIntent(optionalContext: String = "", vararg permissions: GAPermission): GAExpectedIntent {
    return GAExpectedIntent(
            GAIntent.permission,
            GAPermissionValueSpec(
                    optionalContext,
                    permissions.toSet()
            )
    )
}

fun BotBus.linkOutSuggestion(destinationName: CharSequence, url: String): GALinkOutSuggestion {
    val d = translate(destinationName)
    if (d.length > 20) {
        logger.warn { "title $d has more than 20 chars" }
    }
    return GALinkOutSuggestion(d.toString(), url)
}

fun BotBus.suggestion(text: CharSequence): GASuggestion {
    val t = translate(text)
    if (t.length > 25) {
        logger.warn { "title $t has more than 25 chars" }
    }
    return GASuggestion(t.toString())
}

fun BotBus.simpleResponse(text: TextAndVoiceTranslatedString): GASimpleResponse {
    val t = if (text.isSSML()) null else text.voice.toString()
    val s = if (text.isSSML()) text.voice.toString() else null
    val d = text.text.toString()

    return GASimpleResponse(t, s, d)
}

fun BotBus.simpleResponse(text: CharSequence): GASimpleResponse {
    val t = translate(text)
    return if (t is TextAndVoiceTranslatedString) {
        simpleResponse(t)
    } else if (t.isSSML()) {
        flexibleSimpleResponse(ssml = t)
    } else {
        flexibleSimpleResponse(textToSpeech = t)
    }
}

fun BotBus.flexibleSimpleResponse(
        textToSpeech: CharSequence? = null,
        ssml: CharSequence? = null,
        displayText: CharSequence? = null): GASimpleResponse {
    val t = translateAndSetBlankAsNull(textToSpeech)
    val s = translateAndSetBlankAsNull(ssml)
    val d = translateAndSetBlankAsNull(displayText)

    return GASimpleResponse(t, s, d)
}

fun BotBus.item(simpleResponse: GASimpleResponse? = null, basicCard: GABasicCard? = null, structuredResponse: GAStructuredResponse? = null): GAItem {
    return GAItem(simpleResponse, basicCard, structuredResponse)
}

fun BotBus.item(basicCard: GABasicCard? = null): GAItem = item(null, basicCard, null)

fun BotBus.item(simpleResponse: GASimpleResponse? = null): GAItem = item(simpleResponse, null, null)

fun BotBus.basicCard(
        title: String? = null,
        subtitle: String? = null,
        formattedText: String? = null,
        image: GAImage? = null,
        buttons: List<GAButton> = emptyList()): GABasicCard {

    val t = translateAndSetBlankAsNull(title)
    val s = translateAndSetBlankAsNull(subtitle)
    val f = translateAndSetBlankAsNull(formattedText)

    return GABasicCard(t, s, f, image, buttons)
}

fun BotBus.basicCard(title: String? = null, subtitle: String? = null, image: GAImage? = null): GABasicCard = basicCard(title, subtitle, null, image)

fun BotBus.basicCard(title: String? = null, image: GAImage? = null): GABasicCard = basicCard(title, "", null, image)

fun BotBus.basicCard(image: GAImage? = null): GABasicCard = basicCard(null, null, null, image)

fun BotBus.gaImage(url: String, accessibilityText: String, height: Int? = null, width: Int? = null): GAImage {
    val a = translate(accessibilityText)
    return GAImage(url, a.toString(), height, width)
}

fun BotBus.richResponse(items: List<GAItem>, linkOutSuggestion: GALinkOutSuggestion? = null, vararg suggestions: GASuggestion): GARichResponse {
    return GARichResponse(items, linkOutSuggestion = linkOutSuggestion, suggestions = listOf(*suggestions))
}

fun BotBus.richResponse(items: List<GAItem>, vararg suggestions: GASuggestion): GARichResponse
        = richResponse(items, null, *suggestions)

fun BotBus.richResponse(text: CharSequence): GARichResponse
        = richResponse(listOf(item(simpleResponse(text))))

fun BotBus.richResponse(text: CharSequence, linkOutSuggestion: GALinkOutSuggestion? = null): GARichResponse
        = richResponse(listOf(item(simpleResponse(text))), linkOutSuggestion)

fun BotBus.richResponse(item: GAItem, linkOutSuggestion: GALinkOutSuggestion? = null, vararg suggestions: GASuggestion): GARichResponse {
    return richResponse(listOf(item), linkOutSuggestion, *suggestions)
}

fun BotBus.richResponse(basicCard: GABasicCard, linkOutSuggestion: GALinkOutSuggestion? = null, vararg suggestions: GASuggestion): GARichResponse {
    return richResponse(item(basicCard), linkOutSuggestion, *suggestions)
}

fun BotBus.optionValueSpec(simpleSelect: GASimpleSelect? = null,
                           listSelect: GAListSelect? = null,
                           carouselSelect: GACarouselSelect? = null): GAOptionValueSpec {

    return GAOptionValueSpec(simpleSelect, listSelect, carouselSelect)
}


fun BotBus.gaMessage(text: CharSequence,
                     possibleIntents: List<GAExpectedIntent>)
        : GAResponseConnectorMessage = gaMessage(inputPrompt(richResponse(text)), possibleIntents)

fun BotBus.gaMessage(inputPrompt: GAInputPrompt,
                     possibleIntents: List<GAExpectedIntent> = listOf(
                             GAExpectedIntent(GAIntent.text)
                     ),
                     speechBiasingHints: List<String> = emptyList()): GAResponseConnectorMessage {
    return GAResponseConnectorMessage(GAExpectedInput(inputPrompt, possibleIntents, speechBiasingHints))
}

fun BotBus.gaMessage(richResponse: GARichResponse): GAResponseConnectorMessage
        = gaMessage(inputPrompt(richResponse))

fun BotBus.gaMessage(gaRichResponse: GARichResponse, gaChoices: List<GAListItem>): GAResponseConnectorMessage = gaMessage(inputPrompt(gaRichResponse), listOf(
        GAExpectedIntent(GAIntent.text),
        expectedIntentForListSelectOption("", gaChoices)))

fun BotBus.gaMessage(text: String, gaChoices: List<GAListItem>): GAResponseConnectorMessage = gaMessage(inputPrompt(richResponse(text)), listOf(
        GAExpectedIntent(GAIntent.text),
        expectedIntentForListSelectOption("", gaChoices)))

fun BotBus.gaMessage(possibleIntents: List<GAExpectedIntent>): GAResponseConnectorMessage = gaMessage(GAInputPrompt(GARichResponse(emptyList())), possibleIntents)

fun BotBus.inputPrompt(richResponse: GARichResponse): GAInputPrompt
        = GAInputPrompt(richResponse)

fun BotBus.expectedIntentForListSelectOption(title: String, listItems: List<GAListItem>): GAExpectedIntent {
    val t = translate(title)

    return GAExpectedIntent(
            GAIntent.option,
            optionValueSpec(
                    listSelect = GAListSelect(
                            t.toString(),
                            listItems)))
}

private fun BotBus.translateAndSetBlankAsNull(s: CharSequence?): String?
        = translate(s).run { if (isBlank()) null else this.toString() }

fun BotBus.gaChoice(
        title: CharSequence,
        targetIntent: IntentAware,
        vararg parameters: Pair<String, String>)
        : GAListItem
        = gaChoice(title, targetIntent, null, *parameters)

fun BotBus.gaChoice(
        title: CharSequence,
        targetIntent: IntentAware,
        step: StoryStep? = null,
        parameters: Parameters)
        : GAListItem
        = gaChoice(title, targetIntent, step, *parameters.toArray())

fun BotBus.gaChoice(
        title: CharSequence,
        targetIntent: IntentAware,
        step: StoryStep? = null,
        vararg parameters: Pair<String, String>)
        : GAListItem {
    val t = translate(title)
    return GAListItem(GAOptionInfo(
            SendChoice.encodeChoiceId(this, targetIntent, step, parameters.toMap()), listOf(t.toString()))
            , t.toString(), "")
}