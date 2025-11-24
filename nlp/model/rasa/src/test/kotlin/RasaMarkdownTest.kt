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

import ai.tock.nlp.core.Application
import ai.tock.nlp.core.Entity
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.NlpEngineType.Companion.rasa
import ai.tock.nlp.core.sample.SampleEntity
import ai.tock.nlp.core.sample.SampleExpression
import ai.tock.nlp.model.IntentContext
import ai.tock.nlp.rasa.RasaMarkdown
import ai.tock.shared.resourceAsString
import java.util.Locale.FRENCH
import kotlin.test.Test
import kotlin.test.assertEquals

class RasaMarkdownTest {
    private val intent1 =
        Intent(
            "app:intentWithoutEntities",
            emptyList(),
        )

    private val entity1 =
        Entity(
            EntityType("app:e1"),
            "roleE1",
        )

    private val intent2 =
        Intent(
            "app:intentWithOneEntity",
            listOf(
                entity1,
            ),
        )

    private val entity2 =
        Entity(
            EntityType("app:e2"),
            "roleE2",
        )

    private val entity3 =
        Entity(
            EntityType("app:e3"),
            "roleE3",
        )

    private val intent3 =
        Intent(
            "app:intentWithTwoEntities",
            listOf(
                entity2,
                entity3,
            ),
        )

    @Test
    fun testToModelDomainMarkdown() {
        val context =
            IntentContext(
                application =
                    Application(
                        "app",
                        listOf(
                            intent1,
                            intent2,
                            intent3,
                        ),
                        setOf(FRENCH),
                    ),
                language = FRENCH,
                engineType = rasa,
            )

        assertEquals(
            resourceAsString("/domain.yml"),
            RasaMarkdown.toModelDomainMarkdown(context),
        )
    }

    @Test
    fun testToModelNluMarkdown() {
        val samples =
            listOf(
                SampleExpression(
                    "expression 1",
                    intent1,
                ),
                SampleExpression(
                    "expression 2 with entity 1",
                    intent2,
                    listOf(
                        SampleEntity(
                            entity1,
                            emptyList(),
                            18,
                            26,
                        ),
                    ),
                ),
                SampleExpression(
                    "expression 3 with entity 2 hey",
                    intent3,
                    listOf(
                        SampleEntity(
                            entity2,
                            emptyList(),
                            18,
                            26,
                        ),
                    ),
                ),
                SampleExpression(
                    "entity 3 for expression 4",
                    intent3,
                    listOf(
                        SampleEntity(
                            entity3,
                            emptyList(),
                            0,
                            8,
                        ),
                    ),
                ),
            )

        assertEquals(
            resourceAsString("/nlu.yml"),
            RasaMarkdown.toModelNluMarkdown(samples),
        )
    }
}
