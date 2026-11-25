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

package ai.tock.nlp.front.service.storage

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.nlp.front.shared.config.SentencesQueryResult
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.provideOrDefault
import org.litote.kmongo.Id
import java.util.Locale

/**
 * The part of [ClassifiedSentenceDAO] that the NLP API uses at runtime: the parsing and the configuration it caches.
 *
 * A storage that only serves the NLP API (a serverless one) binds this interface. Otherwise, [fromInjector] adapts
 * the bound [ClassifiedSentenceDAO]: the existing storages and the projects that replace them keep working unchanged.
 */
interface ClassifiedSentenceRuntimeDAO {
    fun incrementUnknownStat(
        applicationId: Id<ApplicationDefinition>,
        language: Locale,
        text: String,
    )

    companion object {
        /**
         * The bound [ClassifiedSentenceRuntimeDAO], or the bound [ClassifiedSentenceDAO] adapted to it.
         */
        fun fromInjector(): ClassifiedSentenceRuntimeDAO = injector.provideOrDefault<ClassifiedSentenceRuntimeDAO> { injector.provide<ClassifiedSentenceDAO>().asRuntimeDAO() }
    }
}

/**
 * Adapts this DAO to [ClassifiedSentenceRuntimeDAO].
 */
fun ClassifiedSentenceDAO.asRuntimeDAO(): ClassifiedSentenceRuntimeDAO = ClassifiedSentenceRuntimeDAOAdapter(this)

private class ClassifiedSentenceRuntimeDAOAdapter(
    private val dao: ClassifiedSentenceDAO,
) : ClassifiedSentenceRuntimeDAO {
    override fun incrementUnknownStat(
        applicationId: Id<ApplicationDefinition>,
        language: Locale,
        text: String,
    ) = dao.incrementUnknownStat(applicationId, language, text)
}
