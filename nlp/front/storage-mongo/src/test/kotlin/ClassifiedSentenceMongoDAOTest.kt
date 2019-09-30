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

package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedEntity
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.shared.defaultLocale
import org.junit.jupiter.api.Test
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

/**
 *
 */
class ClassifiedSentenceMongoDAOTest : AbstractTest() {

    val classifiedSentenceDAO: ClassifiedSentenceDAO get() = ClassifiedSentenceMongoDAO

    @Test
    fun `removeSubEntityFromSentences remove sub entities a 2 levels`() {
        val applicationId = newId<ApplicationDefinition>()
        val intentId = newId<IntentDefinition>()
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        val sentence = ClassifiedSentence(
            "a b c a",
            defaultLocale,
            applicationId,
            now,
            now,
            model,
            Classification(
                intentId,
                listOf(
                    ClassifiedEntity(
                        "abc",
                        "abc",
                        0,
                        3,
                        listOf(
                            ClassifiedEntity(
                                "ab",
                                "ab",
                                0,
                                2,
                                listOf(
                                    ClassifiedEntity(
                                        "a",
                                        "a",
                                        0,
                                        1
                                    ),
                                    ClassifiedEntity(
                                        "b",
                                        "b",
                                        1,
                                        2
                                    )
                                )
                            )
                        )
                    ),
                    ClassifiedEntity(
                        "a",
                        "a",
                        3,
                        4
                    )
                )
            ),
            1.0,
            1.0
        )

        classifiedSentenceDAO.save(sentence)

        classifiedSentenceDAO.removeSubEntityFromSentences(applicationId, "ab", "a")

        val s = classifiedSentenceDAO.getSentences(null, null, model).first()

        assertEquals(
            ClassifiedSentence(
                text = "a b c a",
                language = defaultLocale,
                applicationId = applicationId,
                creationDate = now,
                updateDate = s.updateDate,
                status = model,
                classification = Classification(
                    intentId = intentId,
                    entities = listOf(
                        ClassifiedEntity(
                            type = "abc", role = "abc", start = 0, end = 3,
                            subEntities = listOf(
                                ClassifiedEntity(
                                    type = "ab", role = "ab", start = 0, end = 2,
                                    subEntities = listOf(
                                        ClassifiedEntity(type = "b", role = "b", start = 1, end = 2)
                                    )
                                )
                            )
                        ),
                        ClassifiedEntity(
                            "a",
                            "a",
                            3,
                            4
                        )
                    )
                ),
                lastIntentProbability = 1.0,
                lastEntityProbability = 1.0
            ),
            s
        )
    }
}