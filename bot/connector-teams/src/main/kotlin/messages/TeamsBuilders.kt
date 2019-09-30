package ai.tock.bot.connector.teams.messages

import com.microsoft.bot.schema.models.ActionTypes.MESSAGE_BACK
import com.microsoft.bot.schema.models.ActionTypes.OPEN_URL
import com.microsoft.bot.schema.models.CardAction
import com.microsoft.bot.schema.models.CardImage
import ai.tock.bot.connector.teams.teamsConnectorType
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.I18nTranslator

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
    title: CharSequence,
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


fun cardImage(url: String): CardImage = CardImage().withUrl(url)

fun <T : Bus<T>> T.nlpCardAction(
    title: CharSequence
): CardAction =
    translate(title).toString().let { t ->
        CardAction().withTitle(t).withType(MESSAGE_BACK).withDisplayText(t).withText(t)
    }

fun <T : Bus<T>> T.urlCardAction(
    title: CharSequence,
    url: String
): CardAction =
    translate(title).toString().let { t ->
        CardAction().withTitle(t).withType(OPEN_URL).withText(t).withValue(url)
    }

