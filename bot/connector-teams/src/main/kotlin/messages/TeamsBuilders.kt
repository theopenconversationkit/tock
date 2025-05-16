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

package ai.tock.bot.connector.teams.messages

import ai.tock.bot.connector.teams.teamsConnectorType
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.I18nTranslator
import com.microsoft.bot.schema.ActionTypes.IM_BACK
import com.microsoft.bot.schema.ActionTypes.OPEN_URL
import com.microsoft.bot.schema.CardAction
import com.microsoft.bot.schema.CardImage

fun <T : Bus<T>> T.withTeams(messageProvider: () -> TeamsBotMessage): T {
    return withMessage(teamsConnectorType, messageProvider)
}

fun I18nTranslator.teamsMessage(
    text: CharSequence
): TeamsBotTextMessage = TeamsBotTextMessage(translate(text).toString())

fun I18nTranslator.teamsMessageWithButtonCard(
    urlText: CharSequence,
    links: List<CardAction>
): TeamsCardAction = TeamsCardAction(translate(urlText).toString(), links)

fun I18nTranslator.teamsHeroCard(
    title: CharSequence? = null,
    subtitle: CharSequence? = null,
    attachmentContent: CharSequence,
    images: List<CardImage>? = null,
    buttons: List<CardAction>? = null,
    tap: CardAction? = null
): TeamsHeroCard = TeamsHeroCard(
    translate(title).toString(),
    subtitle?.let { translate(subtitle).toString() },
    translate(attachmentContent).toString(),
    images,
    buttons,
    tap
)

fun I18nTranslator.teamsCarousel(
    carouselContent: List<TeamsBotMessage>
): TeamsCarousel = TeamsCarousel(carouselContent)

fun cardImage(url: String): CardImage = CardImage().apply { this.url = url }

fun <T : Bus<T>> T.nlpCardAction(
    title: CharSequence
): CardAction =
    translate(title).toString().let { t ->
        CardAction(IM_BACK, t, t).apply {
            value = t
            text = t
        }
    }

fun <T : Bus<T>> T.urlCardAction(
    title: CharSequence,
    url: String
): CardAction =
    translate(title).toString().let { t ->
        CardAction(OPEN_URL, t).apply {
            value = url
            text = t
        }
    }
