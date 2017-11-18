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

package fr.vsct.tock.nlp.front.shared.config

import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.shared.withNamespace
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.Locale

/**
 *
 */
data class ApplicationDefinition(val name: String,
                                 val namespace: String,
                                 val intents: Set<Id<IntentDefinition>> = emptySet(),
                                 val supportedLocales: Set<Locale> = emptySet(),
                                 val intentStatesMap: Map<Id<IntentDefinition>, Set<String>> = emptyMap(),
                                 val nlpEngineType: NlpEngineType = NlpEngineType.opennlp,
                                 val mergeEngineTypes: Boolean = true,
                                 val supportSubEntities: Boolean = false,
                                 val _id: Id<ApplicationDefinition> = newId()) {

    @Transient
    val qualifiedName: String = name.withNamespace(namespace)

}