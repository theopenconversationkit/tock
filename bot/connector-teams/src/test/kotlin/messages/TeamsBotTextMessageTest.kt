package ai.tock.bot.connector.teams.messages

import com.microsoft.bot.schema.models.ActionTypes
import com.microsoft.bot.schema.models.CardAction
import com.microsoft.bot.schema.models.CardImage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TeamsBotTextMessageTest {

    private val cardAction = CardAction().withDisplayText("displayText").withImage("image").withText("text").withTitle("title").withType(ActionTypes.OPEN_URL).withValue("value")
    private val differentCardAction = CardAction().withDisplayText("displayText").withText("texte").withTitle("title").withType(ActionTypes.MESSAGE_BACK).withValue("value")
    private val cardImage = CardImage().withUrl("http://image.jpeg").withAlt("Image").withTap(cardAction)
    private val cardImageWithJustUrl = CardImage().withUrl("http://image.jpeg")
    private val differentCardImage = CardImage().withUrl("http://image.jpeg").withAlt("Image").withTap(differentCardAction)

    @Test
    fun `assert HeroCardActions are equals`() {
        val heroCard1 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "attachement", images = listOf(cardImage), buttons = listOf(cardAction), tap = cardAction)
        val heroCard2 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "attachement", images = listOf(cardImage), buttons = listOf(cardAction), tap = cardAction)
        val heroCard3 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "attachement", images = listOf(cardImageWithJustUrl), buttons = listOf(cardAction), tap = cardAction)
        val heroCard4 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "attachement", images = listOf(cardImageWithJustUrl), buttons = listOf(cardAction), tap = cardAction)
        val heroCard5 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "attachement", images = listOf(cardImageWithJustUrl), buttons = listOf(cardAction), tap = null)
        val heroCard6 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "attachement", images = listOf(cardImageWithJustUrl), buttons = listOf(cardAction), tap = null)

        assert(heroCard1 == heroCard2)
        assert(heroCard3 == heroCard4)
        assert(heroCard5 == heroCard6)
    }

    @Test
    fun `assert HeroCArdActions are inequals`() {
        val heroCard1 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "attachement", images = listOf(cardImage), buttons = listOf(cardAction), tap = cardAction)
        val heroCard2 = TeamsHeroCard(title = "", subtitle = "sub", attachmentContent = "attachement", images = listOf(cardImage), buttons = listOf(cardAction), tap = cardAction)
        val heroCard3 = TeamsHeroCard(title = "title", subtitle = "", attachmentContent = "attachement", images = listOf(cardImage), buttons = listOf(cardAction), tap = cardAction)
        val heroCard4 = TeamsHeroCard(title = "title", subtitle = null, attachmentContent = "attachement", images = listOf(cardImage), buttons = listOf(cardAction), tap = cardAction)
        val heroCard5 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "", images = listOf(cardImage), buttons = listOf(cardAction), tap = cardAction)
        val heroCard6 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "attachement", images = null, buttons = listOf(cardAction), tap = cardAction)
        val heroCard7 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "attachement", images = listOf(differentCardImage), buttons = listOf(cardAction), tap = cardAction)
        val heroCard8 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "attachement", images = listOf(cardImage, differentCardImage), buttons = listOf(cardAction), tap = cardAction)
        val heroCard9 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "attachement", images = listOf(cardImage, cardImage), buttons = listOf(cardAction), tap = cardAction)
        val heroCard10 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "attachement", images = listOf(cardImage), buttons = null, tap = cardAction)
        val heroCard11 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "attachement", images = listOf(cardImage), buttons = listOf(differentCardAction), tap = cardAction)
        val heroCard12 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "attachement", images = listOf(cardImage), buttons = listOf(cardAction, cardAction), tap = cardAction)
        val heroCard13 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "attachement", images = listOf(cardImage), buttons = listOf(cardAction, cardAction), tap = null)
        val heroCard14 = TeamsHeroCard(title = "title", subtitle = "sub", attachmentContent = "attachement", images = listOf(cardImage), buttons = listOf(cardAction, cardAction), tap = differentCardAction)

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