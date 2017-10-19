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

import emoji4j.EmojiUtils
import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.ga.model.GAIntent
import fr.vsct.tock.bot.connector.ga.model.request.GAPermission
import fr.vsct.tock.bot.connector.ga.model.response.GABasicCard
import fr.vsct.tock.bot.connector.ga.model.response.GAButton
import fr.vsct.tock.bot.connector.ga.model.response.GACarouselItem
import fr.vsct.tock.bot.connector.ga.model.response.GACarouselSelect
import fr.vsct.tock.bot.connector.ga.model.response.GAExpectedInput
import fr.vsct.tock.bot.connector.ga.model.response.GAExpectedIntent
import fr.vsct.tock.bot.connector.ga.model.response.GAFinalResponse
import fr.vsct.tock.bot.connector.ga.model.response.GAImage
import fr.vsct.tock.bot.connector.ga.model.response.GAInputPrompt
import fr.vsct.tock.bot.connector.ga.model.response.GAItem
import fr.vsct.tock.bot.connector.ga.model.response.GALinkOutSuggestion
import fr.vsct.tock.bot.connector.ga.model.response.GAListItem
import fr.vsct.tock.bot.connector.ga.model.response.GAListSelect
import fr.vsct.tock.bot.connector.ga.model.response.GAOpenUrlAction
import fr.vsct.tock.bot.connector.ga.model.response.GAOptionInfo
import fr.vsct.tock.bot.connector.ga.model.response.GAOptionValueSpec
import fr.vsct.tock.bot.connector.ga.model.response.GAPermissionValueSpec
import fr.vsct.tock.bot.connector.ga.model.response.GARichResponse
import fr.vsct.tock.bot.connector.ga.model.response.GASelectItem
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

val gaConnectorType = ConnectorType("ga", textAndVoiceAssistant)

fun BotBus.withGoogleAssistant(messageProvider: () -> ConnectorMessage): BotBus {
    return with(gaConnectorType, messageProvider)
}

fun BotBus.withGoogleVoiceAssistant(messageProvider: () -> ConnectorMessage): BotBus {
    if (userInterfaceType != textChat) {
        with(gaConnectorType, messageProvider)
    }
    return this
}

fun BotBus.gaFinalMessage(richResponse: GARichResponse): GAResponseConnectorMessage
        = GAResponseConnectorMessage(finalResponse = GAFinalResponse(richResponse))

fun BotBus.gaFinalMessage(text: CharSequence? = null): GAResponseConnectorMessage =
        gaFinalMessage(
                if (text == null)
                    //empty rich response
                    richResponse(emptyList())
                else richResponse(text)
        )

fun BotBus.gaMessage(text: CharSequence): GAResponseConnectorMessage
        = gaMessage(inputPrompt(text))

fun BotBus.gaMessage(text: CharSequence, basicCard: GABasicCard): GAResponseConnectorMessage
        = gaMessage(richResponse(listOf(GAItem(simpleResponse(text)), GAItem(basicCard = basicCard))))

fun BotBus.gaMessage(inputPrompt: GAInputPrompt,
                     possibleIntents: List<GAExpectedIntent> = listOf(
                             expectedTextIntent()
                     ),
                     speechBiasingHints: List<String> = emptyList()): GAResponseConnectorMessage
        = GAResponseConnectorMessage(
        GAExpectedInput(
                inputPrompt,
                if (possibleIntents.isEmpty()) listOf(expectedTextIntent()) else possibleIntents,
                speechBiasingHints))


fun BotBus.gaMessage(richResponse: GARichResponse): GAResponseConnectorMessage
        = gaMessage(inputPrompt(richResponse))

fun BotBus.gaMessage(gaRichResponse: GARichResponse, listItems: List<GAListItem>): GAResponseConnectorMessage = gaMessage(inputPrompt(gaRichResponse), listOf(
        expectedTextIntent(),
        expectedIntentForList("", listItems)))

fun BotBus.gaMessage(possibleIntent: GAExpectedIntent): GAResponseConnectorMessage =
        gaMessage(listOf(possibleIntent))

fun BotBus.gaMessage(possibleIntents: List<GAExpectedIntent>): GAResponseConnectorMessage =
        gaMessage(inputPrompt(GARichResponse(emptyList())), possibleIntents)

fun BotBus.gaMessage(text: String, possibleIntents: List<GAExpectedIntent>): GAResponseConnectorMessage =
        gaMessage(
                inputPrompt(richResponse(text)),
                possibleIntents
        )

fun BotBus.gaMessageForList(text: String, items: List<GAListItem>): GAResponseConnectorMessage =
        gaMessage(
                inputPrompt(richResponse(text)), listOf(
                expectedTextIntent(),
                expectedIntentForList("", items))
        )

fun BotBus.gaMessageForCarousel(items: List<GACarouselItem>, suggestions: List<CharSequence> = emptyList()): GAResponseConnectorMessage {
    if (items.size < 2 && items.size > 10) {
        error("must have at least 2 and at most 10 items - current size = ${items.size}")
    } else {
        return gaMessage(
                inputPrompt(richResponse(emptyList(), *suggestions.map { suggestion(it) }.toTypedArray())),
                listOf(
                        expectedTextIntent(),
                        expectedIntentForCarousel(items))
        )
    }
}

/**
 *  Add a basic card if one element to avoid the limitation of 2 items
 */
fun BotBus.gaFlexibleMessageForCarousel(items: List<GACarouselItem>, suggestions: List<CharSequence> = emptyList()): GAResponseConnectorMessage {
    return if (items.size == 1) {
        val one = items.first()
        gaMessage(
                richResponse(
                        basicCard(
                                one.title,
                                null,
                                one.description,
                                one.image
                        ),
                        null,
                        *suggestions.map { suggestion(it) }.toTypedArray())
        )
    } else {
        gaMessageForCarousel(items, suggestions)
    }
}


fun BotBus.permissionIntent(optionalContext: CharSequence = "", vararg permissions: GAPermission): GAExpectedIntent {
    return GAExpectedIntent(
            GAIntent.permission,
            GAPermissionValueSpec(
                    translate(optionalContext).toString(),
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

private fun simpleResponse(textToSpeech: String?, ssml: String?, displayText: String?): GASimpleResponse {
    val ssmlWithoutEmoji = ssml?.removeEmojis()
    val textToSpeechWithoutEmoji = if (ssmlWithoutEmoji.isNullOrBlank()) textToSpeech?.removeEmojis().run { if (isNullOrBlank()) " - " else this } else null
    return GASimpleResponse(textToSpeechWithoutEmoji, ssmlWithoutEmoji, displayText)
}

private fun simpleTextAndVoiceResponse(text: TextAndVoiceTranslatedString): GASimpleResponse {
    val t = if (text.isSSML()) null else text.voice.toString()
    val s = if (text.isSSML()) text.voice.toString() else null
    val d = text.text.toString()

    return simpleResponse(t, s, d)
}

internal fun simpleResponseWithoutTranslate(text: CharSequence): GASimpleResponse {
    return if (text is TextAndVoiceTranslatedString) {
        simpleTextAndVoiceResponse(text)
    } else if (text.isSSML()) {
        flexibleSimpleResponseWithoutTranslate(ssml = text)
    } else {
        flexibleSimpleResponseWithoutTranslate(textToSpeech = text)
    }
}

fun BotBus.simpleResponse(text: CharSequence): GASimpleResponse {
    val t = translate(text)
    return if (t is TextAndVoiceTranslatedString) {
        simpleTextAndVoiceResponse(t)
    } else if (t.isSSML()) {
        flexibleSimpleResponse(ssml = t)
    } else {
        flexibleSimpleResponse(textToSpeech = t)
    }
}

internal fun flexibleSimpleResponseWithoutTranslate(
        textToSpeech: CharSequence? = null,
        ssml: CharSequence? = null,
        displayText: CharSequence? = null): GASimpleResponse {
    val t = textToSpeech.setBlankAsNull()
    val s = ssml.setBlankAsNull()
    val d = displayText.setBlankAsNull()

    return simpleResponse(t, s, d)
}

fun BotBus.flexibleSimpleResponse(
        textToSpeech: CharSequence? = null,
        ssml: CharSequence? = null,
        displayText: CharSequence? = null): GASimpleResponse {
    val t = translateAndSetBlankAsNull(textToSpeech)
    val s = translateAndSetBlankAsNull(ssml)
    val d = translateAndSetBlankAsNull(displayText)

    return simpleResponse(t, s, d)
}

fun BotBus.item(simpleResponse: GASimpleResponse? = null, basicCard: GABasicCard? = null, structuredResponse: GAStructuredResponse? = null): GAItem
        = GAItem(simpleResponse, basicCard, structuredResponse)


fun BotBus.item(basicCard: GABasicCard? = null): GAItem
        = item(null, basicCard, null)

fun BotBus.item(simpleResponse: GASimpleResponse? = null): GAItem
        = item(simpleResponse, null, null)

fun BotBus.basicCard(
        title: CharSequence? = null,
        subtitle: CharSequence? = null,
        formattedText: CharSequence? = null,
        image: GAImage? = null,
        buttons: List<GAButton> = emptyList()): GABasicCard {

    val t = translateAndSetBlankAsNull(title)
    val s = translateAndSetBlankAsNull(subtitle)
    val f = translateAndSetBlankAsNull(formattedText)

    return GABasicCard(t, s, f, image, buttons)
}

fun BotBus.basicCard(title: CharSequence? = null, subtitle: CharSequence? = null, image: GAImage? = null, button: GAButton): GABasicCard
        = basicCard(title, subtitle, null, image, buttons = listOf(button))

fun BotBus.basicCard(title: CharSequence? = null, button: GAButton): GABasicCard
        = basicCard(title, null, null, button)

fun BotBus.basicCard(title: CharSequence? = null, subtitle: CharSequence? = null, image: GAImage? = null): GABasicCard
        = basicCard(title, subtitle, null, image)

fun BotBus.basicCard(title: CharSequence? = null, image: GAImage? = null): GABasicCard
        = basicCard(title, "", null, image)

fun BotBus.basicCard(image: GAImage? = null): GABasicCard
        = basicCard(null, null, null, image)

fun BotBus.gaImage(url: String, accessibilityText: String, height: Int? = null, width: Int? = null): GAImage {
    val a = translate(accessibilityText)
    return GAImage(url, a.toString(), height, width)
}

fun BotBus.richResponse(items: List<GAItem>, linkOutSuggestion: GALinkOutSuggestion? = null, vararg suggestions: GASuggestion): GARichResponse
        = GARichResponse(items, linkOutSuggestion = linkOutSuggestion, suggestions = listOf(*suggestions))


fun BotBus.richResponse(items: List<GAItem>, vararg suggestions: GASuggestion): GARichResponse
        = richResponse(items, null, *suggestions)

fun BotBus.richResponse(text: CharSequence): GARichResponse
        = richResponse(listOf(item(simpleResponse(text))))

fun BotBus.richResponse(text: CharSequence, linkOutSuggestion: GALinkOutSuggestion? = null): GARichResponse
        = richResponse(listOf(item(simpleResponse(text))), linkOutSuggestion)

fun BotBus.richResponse(item: GAItem, linkOutSuggestion: GALinkOutSuggestion? = null, vararg suggestions: GASuggestion): GARichResponse
        = richResponse(listOf(item), linkOutSuggestion, *suggestions)

fun BotBus.richResponse(basicCard: GABasicCard, linkOutSuggestion: GALinkOutSuggestion? = null, vararg suggestions: GASuggestion): GARichResponse
        = richResponse(item(basicCard), linkOutSuggestion, *suggestions)

fun BotBus.richResponse(text: CharSequence, linkOutSuggestion: GALinkOutSuggestion? = null, vararg suggestions: GASuggestion): GARichResponse
        = richResponse(listOf(item(simpleResponse(text))), linkOutSuggestion, *suggestions)

fun BotBus.richResponse(text: CharSequence, basicCard: GABasicCard, linkOutSuggestion: GALinkOutSuggestion? = null, vararg suggestions: GASuggestion): GARichResponse
        = richResponse(listOf(item(simpleResponse(text)), item(basicCard)), linkOutSuggestion, *suggestions)

fun BotBus.optionValueSpec(simpleSelect: GASimpleSelect? = null,
                           listSelect: GAListSelect? = null,
                           carouselSelect: GACarouselSelect? = null): GAOptionValueSpec
        = GAOptionValueSpec(simpleSelect, listSelect, carouselSelect)

fun BotBus.inputPrompt(text: CharSequence, linkOutSuggestion: GALinkOutSuggestion? = null, noInputPrompts: List<GASimpleResponse> = emptyList()): GAInputPrompt
        = inputPrompt(richResponse(text, linkOutSuggestion),noInputPrompts)

fun BotBus.inputPrompt(richResponse: GARichResponse , noInputPrompts: List<GASimpleResponse> = emptyList()): GAInputPrompt
        = GAInputPrompt(richResponse , noInputPrompts)

fun BotBus.expectedIntentForList(title: String, items: List<GAListItem>): GAExpectedIntent {
    val t = translate(title)

    return GAExpectedIntent(
            GAIntent.option,
            optionValueSpec(
                    listSelect = GAListSelect(
                            t.toString(),
                            items)))
}

fun BotBus.expectedIntentForCarousel(items: List<GACarouselItem>): GAExpectedIntent {
    return GAExpectedIntent(
            GAIntent.option,
            optionValueSpec(carouselSelect = GACarouselSelect(items)
            )
    )
}

fun BotBus.expectedIntentForSimpleSelect(items: List<GASelectItem>): GAExpectedIntent {
    return GAExpectedIntent(
            GAIntent.option,
            optionValueSpec(simpleSelect = GASimpleSelect(items)
            )
    )
}

fun BotBus.gaButton(title: CharSequence, url: String): GAButton {
    return GAButton(translate(title).toString(), GAOpenUrlAction(url))
}

private fun BotBus.translateAndSetBlankAsNull(s: CharSequence?): String?
        = translate(s).run { setBlankAsNull() }

private fun CharSequence?.setBlankAsNull(): String?
        = if (isNullOrBlank()) null else toString()

fun BotBus.listItem(
        title: CharSequence,
        targetIntent: IntentAware,
        vararg parameters: Pair<String, String>)
        : GAListItem
        = listItem(title, targetIntent, null, *parameters)

fun BotBus.listItem(
        title: CharSequence,
        targetIntent: IntentAware,
        step: StoryStep? = null,
        parameters: Parameters)
        : GAListItem
        = listItem(title, targetIntent, step, *parameters.toArray())

fun BotBus.listItem(
        title: CharSequence,
        targetIntent: IntentAware,
        step: StoryStep? = null,
        vararg parameters: Pair<String, String>)
        : GAListItem {
    val t = translate(title)
    return GAListItem(
            optionInfo(
                    t,
                    targetIntent,
                    step,
                    *parameters
            ),
            t.toString(),
            "")
}

fun BotBus.optionInfo(
        title: CharSequence,
        targetIntent: IntentAware,
        step: StoryStep? = null,
        vararg parameters: Pair<String, String>
): GAOptionInfo {
    val t = translate(title)
    //add the title to the parameters as we need to double check the title in WebhookActionConverter
    val map = parameters.toMap() + (SendChoice.TITLE_PARAMETER to t.toString())
    return GAOptionInfo(
            SendChoice.encodeChoiceId(
                    this,
                    targetIntent,
                    step,
                    map),
            listOf(t.toString())
    )
}

fun BotBus.carouselItem(
        targetIntent: IntentAware,
        title: CharSequence,
        description: CharSequence? = null,
        image: GAImage? = null,
        vararg parameters: Pair<String, String>)
        : GACarouselItem
        = carouselItem(targetIntent, null, title, description, image, *parameters)

fun BotBus.carouselItem(
        targetIntent: IntentAware,
        step: StoryStep? = null,
        title: CharSequence,
        description: CharSequence? = null,
        image: GAImage? = null,
        parameters: Parameters)
        : GACarouselItem
        = carouselItem(targetIntent, step, title, description, image, *parameters.toArray())

fun BotBus.carouselItem(
        targetIntent: IntentAware,
        step: StoryStep? = null,
        title: CharSequence,
        description: CharSequence? = null,
        image: GAImage? = null,
        vararg parameters: Pair<String, String>)
        : GACarouselItem {
    val t = translate(title)
    return GACarouselItem(
            optionInfo(
                    t,
                    targetIntent,
                    step,
                    *parameters
            ),
            t.toString(),
            translate(description).toString(),
            image
    )
}

fun BotBus.selectItem(
        title: CharSequence,
        targetIntent: IntentAware,
        vararg parameters: Pair<String, String>)
        : GASelectItem
        = selectItem(title, targetIntent, null, null, *parameters)

fun BotBus.selectItem(
        title: CharSequence,
        targetIntent: IntentAware,
        optionTitle: CharSequence? = null,
        vararg parameters: Pair<String, String>)
        : GASelectItem
        = selectItem(title, targetIntent, null, optionTitle, *parameters)

fun BotBus.selectItem(
        title: CharSequence,
        targetIntent: IntentAware,
        step: StoryStep? = null,
        optionTitle: CharSequence? = null,
        parameters: Parameters)
        : GASelectItem
        = selectItem(title, targetIntent, step, optionTitle, *parameters.toArray())

fun BotBus.selectItem(
        title: CharSequence,
        targetIntent: IntentAware,
        step: StoryStep? = null,
        optionTitle: CharSequence? = null,
        vararg parameters: Pair<String, String>)
        : GASelectItem {
    return GASelectItem(
            optionInfo(
                    title,
                    targetIntent,
                    step,
                    *parameters
            ),
            optionTitle?.let { translate(it).toString() }
    )
}

fun expectedTextIntent(): GAExpectedIntent = GAExpectedIntent(GAIntent.text)


internal fun String.endWithPunctuation(): Boolean
        = endsWith(".") || endsWith("!") || endsWith("?") || endsWith(",") || endsWith(";") || endsWith(":")

internal fun concat(s1: String?, s2: String?): String {
    val s = s1?.trim() ?: ""
    return s + (if (s.isEmpty() || s.endWithPunctuation()) " " else ". ") + (s2?.trim() ?: "")
}

internal fun String.removeEmojis(): String =
        EmojiUtils.removeAllEmojis(
                EmojiUtils.emojify(this)
                        .replace("\uD83D\uDC68", ":3")
                        .replace("\uD83D\uDE2E", ":0")
        )