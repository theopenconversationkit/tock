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

package fr.vsct.tock.nlp.admin.model

import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.Locale

/**
 *
 */
data class ApplicationWithIntents(
    val name: String,
    val namespace: String,
    val intents: List<IntentDefinition>,
    val supportedLocales: Set<Locale>,
    val nlpEngineType: NlpEngineType,
    val mergeEngineTypes: Boolean = true,
    val useEntityModels: Boolean = true,
    val supportSubEntities: Boolean = false,
    val _id: Id<ApplicationDefinition>?
) {

    constructor(application: ApplicationDefinition, intents: List<IntentDefinition>) :
            this(
                application.name,
                application.namespace,
                intents.sortedBy { it.name },
                application.supportedLocales,
                application.nlpEngineType,
                application.mergeEngineTypes,
                application.useEntityModels,
                application.supportSubEntities,
                application._id
            )

    fun toApplication(): ApplicationDefinition {
        return ApplicationDefinition(
            name,
            namespace,
            intents.map { it._id }.toSet(),
            supportedLocales,
            emptyMap(),
            nlpEngineType,
            mergeEngineTypes,
            useEntityModels,
            supportSubEntities,
            _id ?: newId()
        )
    }

}