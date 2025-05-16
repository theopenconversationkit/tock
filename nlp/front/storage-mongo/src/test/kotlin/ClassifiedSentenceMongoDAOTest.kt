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

package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedEntity
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.shared.Dice.newId
import ai.tock.shared.defaultLocale
import org.junit.jupiter.api.Test
import org.litote.kmongo.newId
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

/**
 *
 */
class ClassifiedSentenceMongoDAOTest : AbstractTest() {

    private val classifiedSentenceDAO: ClassifiedSentenceDAO get() = ClassifiedSentenceMongoDAO
    private val entityTypeDAO: EntityTypeDefinitionDAO get() = EntityTypeDefinitionMongoDAO

    private val applicationId = newId<ApplicationDefinition>()
    private val intentId = newId<IntentDefinition>()
    private val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
    private val namespace = "test"
    private val entityTypeA = EntityTypeDefinition("$namespace:a")
    private val entityTypeB = EntityTypeDefinition("$namespace:a")
    private val entityTypeAB = EntityTypeDefinition(
        "$namespace:ab",
        subEntities = listOf(
            EntityDefinition(entityTypeA, "a"),
            EntityDefinition(entityTypeB, "b")
        )
    )

    private val entityTypeABC = EntityTypeDefinition(
        "$namespace:abc",
        subEntities = listOf(EntityDefinition(entityTypeAB, "ab"))
    )

    private val sentence = ClassifiedSentence(
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
                    entityTypeABC.name,
                    "abc",
                    0,
                    3,
                    listOf(
                        ClassifiedEntity(
                            entityTypeAB.name,
                            "ab",
                            0,
                            2,
                            listOf(
                                ClassifiedEntity(
                                    entityTypeA.name,
                                    "a",
                                    0,
                                    1
                                ),
                                ClassifiedEntity(
                                    entityTypeB.name,
                                    "b",
                                    1,
                                    2
                                )
                            )
                        )
                    )
                ),
                ClassifiedEntity(
                    entityTypeA.name,
                    "a",
                    3,
                    4
                )
            )
        ),
        1.0,
        1.0
    )

    private fun waitForSentence(count: Long = 0L): ClassifiedSentence {
        when {
            count == 0L -> Thread.sleep(100L)
            count < 100 -> Thread.sleep(count)
            else -> error("no sentence found")
        }
        return classifiedSentenceDAO.search(
            SentencesQuery(
                applicationId = applicationId,
                language = sentence.language,
                search = sentence.text
            )
        ).sentences.firstOrNull() ?: waitForSentence(count + 1)
    }

    @Test
    fun `switchSentencesEntity switch entity and create sub entity type if needed`() {
        entityTypeDAO.save(entityTypeABC)
        entityTypeDAO.save(entityTypeB)
        classifiedSentenceDAO.save(sentence)

        classifiedSentenceDAO.switchSentencesEntity(
            namespace,
            listOf(sentence),
            EntityDefinition(entityTypeABC, "abc"),
            EntityDefinition(entityTypeB, "b")
        )

        // check
        assertEquals(entityTypeB.name, waitForSentence().classification.entities.first().type)

        entityTypeDAO.getEntityTypeByName(entityTypeB.name)!!.apply {
            assertEquals(entityTypeABC.subEntities.map { it.role }, subEntities.map { it.role })
        }
    }

    @Test
    fun `removeSubEntityFromSentences remove sub entities a 2 levels`() {
        classifiedSentenceDAO.save(sentence)

        classifiedSentenceDAO.removeSubEntityFromSentences(applicationId, entityTypeAB.name, "a")

        val s = waitForSentence()

        assertEquals(
            ClassifiedSentence(
                text = sentence.text,
                language = defaultLocale,
                applicationId = applicationId,
                creationDate = now,
                updateDate = s.updateDate,
                status = model,
                classification = Classification(
                    intentId = intentId,
                    entities = listOf(
                        ClassifiedEntity(
                            type = entityTypeABC.name, role = "abc", start = 0, end = 3,
                            subEntities = listOf(
                                ClassifiedEntity(
                                    type = entityTypeAB.name, role = "ab", start = 0, end = 2,
                                    subEntities = listOf(
                                        ClassifiedEntity(type = entityTypeB.name, role = "b", start = 1, end = 2)
                                    )
                                )
                            )
                        ),
                        ClassifiedEntity(
                            entityTypeA.name,
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
