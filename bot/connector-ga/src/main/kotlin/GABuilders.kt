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

package ai.tock.bot.connector.ga

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ga.model.GAIntent
import ai.tock.bot.connector.ga.model.request.GAPermission
import ai.tock.bot.connector.ga.model.response.GABasicCard
import ai.tock.bot.connector.ga.model.response.GAButton
import ai.tock.bot.connector.ga.model.response.GACarouselSelect
import ai.tock.bot.connector.ga.model.response.GAExpectedInput
import ai.tock.bot.connector.ga.model.response.GAExpectedIntent
import ai.tock.bot.connector.ga.model.response.GAFinalResponse
import ai.tock.bot.connector.ga.model.response.GAImage
import ai.tock.bot.connector.ga.model.response.GAInputPrompt
import ai.tock.bot.connector.ga.model.response.GAItem
import ai.tock.bot.connector.ga.model.response.GALinkOutSuggestion
import ai.tock.bot.connector.ga.model.response.GAListSelect
import ai.tock.bot.connector.ga.model.response.GAOpenUrlAction
import ai.tock.bot.connector.ga.model.response.GAOptionInfo
import ai.tock.bot.connector.ga.model.response.GAOptionValueSpec
import ai.tock.bot.connector.ga.model.response.GAPermissionValueSpec
import ai.tock.bot.connector.ga.model.response.GARichResponse
import ai.tock.bot.connector.ga.model.response.GASimpleResponse
import ai.tock.bot.connector.ga.model.response.GASimpleSelect
import ai.tock.bot.connector.ga.model.response.GAStructuredResponse
import ai.tock.bot.connector.ga.model.response.GAUpdatePermissionValueSpec
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.StoryStepDef
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.I18nTranslator
import ai.tock.bot.engine.action.SendChoice
import ai.tock.translator.UserInterfaceType.textAndVoiceAssistant
import ai.tock.translator.UserInterfaceType.textChat
import com.vdurmont.emoji.EmojiParser
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

internal const val GA_CONNECTOR_TYPE_ID = "ga"

/**
 * The Google Assistant [ConnectorType].
 */
val gaConnectorType = ConnectorType(GA_CONNECTOR_TYPE_ID, textAndVoiceAssistant)

/**
 * Sends a Google Assistant message only if the [ConnectorType] of the current [BotBus] is [gaConnectorType].
 */
fun <T : Bus<T>> T.sendToGoogleAssistant(
    messageProvider: T.() -> GAResponseConnectorMessage,
    delay: Long = defaultDelay(currentAnswerIndex),
): T {
    if (targetConnectorType == gaConnectorType) {
        withMessage(messageProvider(this))
        send(delay)
    }
    return this
}

/**
 * Sends a Google Assistant message as last bot answer, only if the [ConnectorType] of the current [BotBus] is [gaConnectorType].
 */
fun <T : Bus<T>> T.endForGoogleAssistant(
    messageProvider: T.() -> GAResponseConnectorMessage,
    delay: Long = defaultDelay(currentAnswerIndex),
): T {
    if (targetConnectorType == gaConnectorType) {
        withMessage(messageProvider(this))
        end(delay)
    }
    return this
}

/**
 * Adds a Google Assistant [ConnectorMessage] if the current connector is Google Assistant.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun <T : Bus<T>> T.withGoogleAssistant(messageProvider: () -> GAResponseConnectorMessage): T {
    return withMessage(gaConnectorType, messageProvider)
}

/**
 * If the device supports audio, adds a Google Assistant [ConnectorMessage] if the current connector is Google Assistant.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun <T : Bus<T>> T.withGoogleVoiceAssistant(messageProvider: () -> GAResponseConnectorMessage): T {
    if (userInterfaceType != textChat) {
        withMessage(gaConnectorType, messageProvider)
    }
    return this
}

/**
 * Final Google Assistant message (end of conversation).
 */
fun I18nTranslator.gaFinalMessage(richResponse: GARichResponse): GAResponseConnectorMessage = GAResponseConnectorMessage(finalResponse = GAFinalResponse(richResponse))

/**
 * Final Google Assistant message (end of conversation).
 */
fun I18nTranslator.gaFinalMessage(text: CharSequence? = null): GAResponseConnectorMessage =
    gaFinalMessage(
        if (text == null) {
            // empty rich response
            richResponse(emptyList())
        } else {
            richResponse(text)
        },
    )

/**
 * Google Assistant Message with all available parameters.
 */
fun I18nTranslator.gaMessage(
    inputPrompt: GAInputPrompt,
    possibleIntents: List<GAExpectedIntent> =
        listOf(
            expectedTextIntent(),
        ),
    speechBiasingHints: List<String> = emptyList(),
): GAResponseConnectorMessage =
    GAResponseConnectorMessage(
        GAExpectedInput(
            inputPrompt,
            if (possibleIntents.isEmpty()) listOf(expectedTextIntent()) else possibleIntents,
            speechBiasingHints,
        ),
    )

fun gaLogout(): GAResponseConnectorMessage =
    GAResponseConnectorMessage(
        logoutEvent = true,
    )

/**
 * Google Assistant Message with suggestions.
 */
fun I18nTranslator.gaMessage(
    text: CharSequence,
    vararg suggestions: CharSequence,
): GAResponseConnectorMessage = if (suggestions.isEmpty()) gaMessage(inputPrompt(text)) else gaMessage(richResponse(text, *suggestions))

/**
 * Google Assistant Message with [GABasicCard].
 */
fun I18nTranslator.gaMessage(
    text: CharSequence,
    basicCard: GABasicCard,
): GAResponseConnectorMessage = gaMessage(richResponse(listOf(GAItem(simpleResponse(text)), GAItem(basicCard = basicCard))))

/**
 * Google Assistant Message with [GARichResponse].
 */
fun I18nTranslator.gaMessage(richResponse: GARichResponse): GAResponseConnectorMessage = gaMessage(inputPrompt(richResponse))

/**
 * Google Assistant Message with one [GAExpectedIntent].
 */
fun I18nTranslator.gaMessage(possibleIntent: GAExpectedIntent): GAResponseConnectorMessage = gaMessage(listOf(possibleIntent))

/**
 * Google Assistant Message with multiple [GAExpectedIntent].
 */
fun I18nTranslator.gaMessage(possibleIntents: List<GAExpectedIntent>): GAResponseConnectorMessage = gaMessage(inputPrompt(GARichResponse(emptyList())), possibleIntents)

/**
 * Google Assistant Message with text and multiple [GAExpectedIntent].
 */
fun I18nTranslator.gaMessage(
    text: CharSequence,
    possibleIntents: List<GAExpectedIntent>,
): GAResponseConnectorMessage =
    gaMessage(
        inputPrompt(richResponse(text)),
        possibleIntents,
    )

/**
 * Google Assistant Message asking for [GAPermission].
 */
fun I18nTranslator.permissionIntent(
    optionalContext: CharSequence? = null,
    vararg permissions: GAPermission,
): GAExpectedIntent {
    return GAExpectedIntent(
        GAIntent.permission,
        GAPermissionValueSpec(
            translate(optionalContext).toString(),
            permissions.toSet(),
        ),
    )
}

/**
 * Google Assistant Message asking for update [GAPermission].
 */
fun I18nTranslator.updatePermissionIntent(intent: String): GAExpectedIntent {
    return GAExpectedIntent(
        GAIntent.permission,
        GAPermissionValueSpec(
            null,
            setOf(GAPermission.UPDATE),
            GAUpdatePermissionValueSpec(intent),
        ),
    )
}

/**
 * Provides a [GALinkOutSuggestion].
 */
fun I18nTranslator.linkOutSuggestion(
    destinationName: CharSequence,
    url: String,
): GALinkOutSuggestion {
    val d = translate(destinationName)
    if (d.length > 20) {
        logger.warn { "title $d has more than 20 chars" }
    }
    return GALinkOutSuggestion(d.toString(), url)
}

/**
 * Provides a [GAItem] with all available parameters.
 */
fun I18nTranslator.item(
    simpleResponse: GASimpleResponse? = null,
    basicCard: GABasicCard? = null,
    structuredResponse: GAStructuredResponse? = null,
): GAItem = GAItem(simpleResponse, basicCard, structuredResponse)

/**
 * Provides a [GAItem] with a [GABasicCard].
 */
fun I18nTranslator.item(basicCard: GABasicCard): GAItem = item(null, basicCard, null)

/**
 * Provides a [GAItem] with a [GAStructuredResponse].
 */
fun I18nTranslator.item(structuredResponse: GAStructuredResponse): GAItem = item(null, null, structuredResponse)

/**
 * Provides a [GAItem] with a [GASimpleResponse].
 */
fun I18nTranslator.item(simpleResponse: GASimpleResponse): GAItem = item(simpleResponse, null, null)

/**
 * Provides a [GAImage] with all available parameters.
 */
fun I18nTranslator.gaImage(
    url: String,
    accessibilityText: CharSequence,
    height: Int? = null,
    width: Int? = null,
): GAImage {
    val a = translate(accessibilityText)
    return GAImage(url, a.toString(), height, width)
}

/**
 * Provides a [GAOptionValueSpec] with all available parameters.
 */
fun I18nTranslator.optionValueSpec(
    simpleSelect: GASimpleSelect? = null,
    listSelect: GAListSelect? = null,
    carouselSelect: GACarouselSelect? = null,
): GAOptionValueSpec = GAOptionValueSpec(simpleSelect, listSelect, carouselSelect)

/**
 * Provides a [GAInputPrompt] with all available parameters.
 */
fun I18nTranslator.inputPrompt(
    richResponse: GARichResponse,
    noInputPrompts: List<GASimpleResponse> = emptyList(),
): GAInputPrompt = GAInputPrompt(richResponse, noInputPrompts)

/**
 * Provides a [GAInputPrompt] with a simple [GARichResponse] builder.
 */
fun I18nTranslator.inputPrompt(
    text: CharSequence,
    linkOutSuggestion: GALinkOutSuggestion? = null,
    noInputPrompts: List<GASimpleResponse> = emptyList(),
): GAInputPrompt = inputPrompt(richResponse(text, linkOutSuggestion), noInputPrompts)

/**
 * Provides a [GAButton].
 */
fun I18nTranslator.gaButton(
    title: CharSequence,
    url: String,
): GAButton {
    return GAButton(translate(title).toString(), GAOpenUrlAction(url))
}

/**
 * Provides a [GAOptionInfo] with all available parameters.
 */
fun <T : Bus<T>> T.optionInfo(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStepDef? = null,
    vararg parameters: Pair<String, String>,
): GAOptionInfo {
    val t = translate(title)
    // add the title to the parameters as we need to double check the title in WebhookActionConverter
    val map = parameters.toMap() + (SendChoice.TITLE_PARAMETER to t.toString())
    return GAOptionInfo(
        SendChoice.encodeChoiceId(
            this,
            targetIntent,
            step,
            map,
        ),
        listOf(t.toString()),
    )
}

/**
 * The common [GAIntent.text] [GAExpectedIntent].
 */
fun expectedTextIntent(): GAExpectedIntent = GAExpectedIntent(GAIntent.text)

internal fun String.endWithPunctuation(): Boolean = endsWith(".") || endsWith("!") || endsWith("?") || endsWith(",") || endsWith(";") || endsWith(":")

internal fun concat(
    s1: String?,
    s2: String?,
): String {
    val s = s1?.trim() ?: ""
    return s + (if (s.isEmpty() || s.endWithPunctuation()) " " else ". ") + (s2?.trim() ?: "")
}

internal fun String.removeEmojis(): String =
    EmojiParser.removeAllEmojis(
        EmojiParser.parseToUnicode(this.replace("://", "_____"))
            .replace("\uD83D\uDC68", ":3")
            .replace("\uD83D\uDE2E", ":0")
            .replace("_____", "://"),
    )
