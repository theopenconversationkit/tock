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

package fr.vsct.tock.bot.engine

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import fr.vsct.tock.bot.admin.bot.BotApplicationConfigurationDAO
import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserLock
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.bot.engine.user.UserTimeline
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import fr.vsct.tock.shared.injector
import fr.vsct.tock.translator.I18nDAO
import fr.vsct.tock.translator.TranslatorEngine
import org.junit.Test

/**
 *
 */
class BotTest {

    val userLock: UserLock = mock()
    val userTimelineDAO: UserTimelineDAO = mock()
    val botConfDAO: BotApplicationConfigurationDAO = mock()
    val i18nDAO: I18nDAO = mock()
    val translator: TranslatorEngine = mock {
        on { translate(any(), any(), any()) } doReturn ("ok")
    }

    val nlp: NlpController = mock()
    val connector: Connector = mock {
        on { loadProfile(any(), any()) } doReturn (UserPreferences())
        on { connectorType } doReturn (ConnectorType("test"))
    }

    val defaultSentence = SendSentence(
            PlayerId("id"),
            "applicationId",
            PlayerId("bot", PlayerType.bot),
            "ok computer"
    )

    val userTimeline = UserTimeline(
            PlayerId("id")
    )

    init {
        injector.inject(Kodein {
            bind<NlpController>() with provider { nlp }

            bind<UserLock>() with provider { userLock }
            bind<UserTimelineDAO>() with provider { userTimelineDAO }
            bind<I18nDAO>() with provider { i18nDAO }
            bind<TranslatorEngine>() with provider { translator }
            bind<BotApplicationConfigurationDAO>() with provider { botConfDAO }
        })
    }

    @Test
    fun handleSendSentence_whenWaitingRawInput_shouldNotSendNlpQuery() {

        val bot = Bot(BotDefinitionTest())

        bot.handle(
                defaultSentence,
                userTimeline,
                ConnectorController(bot, connector, BotVerticle())
        )

        verify(nlp).parseSentence(any(), any(), any(), any(), any())
    }

    @Test
    fun handleSendSentence_whenNotWaitingRawInput_shouldSendNlpQuery() {
        val bot = Bot(BotDefinitionTest())
        userTimeline.userState.waitingRawInput = true

        bot.handle(
                defaultSentence,
                userTimeline,
                ConnectorController(bot, connector, BotVerticle())
        )

        verify(nlp, never()).parseSentence(any(), any(), any(), any(), any())
    }
}