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

import com.microsoft.bot.schema.ActionTypes.IM_BACK
import com.microsoft.bot.schema.ActionTypes.OPEN_URL
import com.microsoft.bot.schema.CardAction
import com.microsoft.bot.schema.CardImage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TeamsBotTextMessageTest {

    private val cardAction = CardAction(OPEN_URL, "title").apply {
        displayText = "displayText"
        image = "image"
        text = "text"
        value = "value"
    }
    private val differentCardAction = CardAction(IM_BACK, "title").apply {
        displayText = "displayText"
        text = "text"
        value = "value"
    }
    private val cardImage = CardImage().apply {
        url = "http://image.jpeg"
        alt = "image"
        tap = cardAction
    }
    private val cardImageWithJustUrl = CardImage().apply {
        url = "http://image.jpeg"
    }
    private val differentCardImage = CardImage().apply {
        url = "http://image.jpeg"
        alt = "Image"
        tap = differentCardAction
    }

    @Test
    fun `assert HeroCardActions are equals`() {
        val heroCard1 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = listOf(cardImage),
            buttons = listOf(cardAction),
            tap = cardAction
        )
        val heroCard2 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = listOf(cardImage),
            buttons = listOf(cardAction),
            tap = cardAction
        )
        val heroCard3 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = listOf(cardImageWithJustUrl),
            buttons = listOf(cardAction),
            tap = cardAction
        )
        val heroCard4 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = listOf(cardImageWithJustUrl),
            buttons = listOf(cardAction),
            tap = cardAction
        )
        val heroCard5 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = listOf(cardImageWithJustUrl),
            buttons = listOf(cardAction),
            tap = null
        )
        val heroCard6 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = listOf(cardImageWithJustUrl),
            buttons = listOf(cardAction),
            tap = null
        )

        assert(heroCard1 == heroCard2)
        assert(heroCard3 == heroCard4)
        assert(heroCard5 == heroCard6)
    }

    @Test
    fun `assert HeroCArdActions are inequals`() {
        val heroCard1 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = listOf(cardImage),
            buttons = listOf(cardAction),
            tap = cardAction
        )
        val heroCard2 = TeamsHeroCard(
            title = "",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = listOf(cardImage),
            buttons = listOf(cardAction),
            tap = cardAction
        )
        val heroCard3 = TeamsHeroCard(
            title = "title",
            subtitle = "",
            attachmentContent = "attachement",
            images = listOf(cardImage),
            buttons = listOf(cardAction),
            tap = cardAction
        )
        val heroCard4 = TeamsHeroCard(
            title = "title",
            subtitle = null,
            attachmentContent = "attachement",
            images = listOf(cardImage),
            buttons = listOf(cardAction),
            tap = cardAction
        )
        val heroCard5 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "",
            images = listOf(cardImage),
            buttons = listOf(cardAction),
            tap = cardAction
        )
        val heroCard6 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = null,
            buttons = listOf(cardAction),
            tap = cardAction
        )
        val heroCard7 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = listOf(differentCardImage),
            buttons = listOf(cardAction),
            tap = cardAction
        )
        val heroCard8 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = listOf(cardImage, differentCardImage),
            buttons = listOf(cardAction),
            tap = cardAction
        )
        val heroCard9 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = listOf(cardImage, cardImage),
            buttons = listOf(cardAction),
            tap = cardAction
        )
        val heroCard10 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = listOf(cardImage),
            buttons = null,
            tap = cardAction
        )
        val heroCard11 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = listOf(cardImage),
            buttons = listOf(differentCardAction),
            tap = cardAction
        )
        val heroCard12 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = listOf(cardImage),
            buttons = listOf(cardAction, cardAction),
            tap = cardAction
        )
        val heroCard13 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = listOf(cardImage),
            buttons = listOf(cardAction, cardAction),
            tap = null
        )
        val heroCard14 = TeamsHeroCard(
            title = "title",
            subtitle = "sub",
            attachmentContent = "attachement",
            images = listOf(cardImage),
            buttons = listOf(cardAction, cardAction),
            tap = differentCardAction
        )

        assert(heroCard1 != heroCard2)
        assert(heroCard1 != heroCard3)
        assert(heroCard1 != heroCard4)
        assert(heroCard1 != heroCard5)
        assert(heroCard1 != heroCard6)
        assert(heroCard1 != heroCard7)
        assert(heroCard1 != heroCard8)
        assert(heroCard1 != heroCard9)
        assert(heroCard1 != heroCard10)
        assert(heroCard1 != heroCard11)
        assert(heroCard1 != heroCard12)
        assert(heroCard1 != heroCard13)
        assert(heroCard1 != heroCard14)
    }
}
