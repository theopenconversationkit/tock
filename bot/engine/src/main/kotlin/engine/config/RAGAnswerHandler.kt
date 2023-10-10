/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.engine.config

import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.FootNote
import ai.tock.bot.engine.action.SendSentenceWithFootnotes
import engine.config.AbstractProactiveAnswerHandler

class RAGAnswerHandler : AbstractProactiveAnswerHandler {
    override fun handleProactiveAnswer(botBus: BotBus) {
        with(botBus) {
            // TODO MASS : to be finalised once python stack (RAG Agent) is ready
            send("blabla")
            sendDebugData("testddd", listOf("une", "deux"))
            flushProactiveConversation()
            send("un autre blabla")
            flushProactiveConversation()

            // Appel LLM
            send(
                SendSentenceWithFootnotes(
                    botId,
                    applicationId,
                    userId,
                    text = "Bénéficiez d’une assurance personnalisée : propriétaire ou locataire, chacun de vos besoins trouvera sa garantie. Avec votre assurance, protégez vos biens tout en favorisant l'économie circulaire.",
                    footNotes = mutableListOf(
                        FootNote(
                            "1",
                            "Assurance Habitation",
                            "https://www.cmso.com/reseau-bancaire-cooperatif/web/assurances/assurances-habitation/assurance-habitation"
                        ),
                        FootNote(
                            "2",
                            "Assurance Habitation Étudiant",
                            "https://www.cmso.com/reseau-bancaire-cooperatif/web/assurances/assurances-habitation/assurance-habitation-etudiant"
                        ),
                        FootNote(
                            "3",
                            "Télésurveillance",
                            "https://www.cmso.com/reseau-bancaire-cooperatif/web/assurances/assurances-habitation/telesurveillance"
                        )
                    )
                )
            )
        }
    }
}