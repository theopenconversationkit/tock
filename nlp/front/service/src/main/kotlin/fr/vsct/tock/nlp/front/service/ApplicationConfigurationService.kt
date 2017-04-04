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

package fr.vsct.tock.nlp.front.service

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.nlp.core.Intent.Companion.unknownIntent
import fr.vsct.tock.nlp.front.service.FrontRepository.toEntityType
import fr.vsct.tock.nlp.front.service.storage.ApplicationDefinitionDAO
import fr.vsct.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import fr.vsct.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import fr.vsct.tock.nlp.front.service.storage.IntentDefinitionDAO
import fr.vsct.tock.nlp.front.shared.ApplicationConfiguration
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.namespaceAndName

val applicationDAO: ApplicationDefinitionDAO by injector.instance()
val entityTypeDAO: EntityTypeDefinitionDAO by injector.instance()
val intentDAO: IntentDefinitionDAO by injector.instance()
val sentenceDAO: ClassifiedSentenceDAO by injector.instance()

/**
 *
 */
object ApplicationConfigurationService :
        ApplicationDefinitionDAO by applicationDAO,
        EntityTypeDefinitionDAO by entityTypeDAO,
        IntentDefinitionDAO by intentDAO,
        ClassifiedSentenceDAO by sentenceDAO,
        ApplicationConfiguration {

    override fun save(entityType: EntityTypeDefinition) {
        entityTypeDAO.save(entityType)
        FrontRepository.entityTypes.put(entityType.name, toEntityType(entityType))
    }

    override fun getIntentIdByQualifiedName(name: String): String {
        return if (name == unknownIntent)
            unknownIntent
        else name.namespaceAndName().run { intentDAO.getIntentByNamespaceAndName(first, second)!!._id!! }
    }

    override fun initData() {
        FrontRepository.registerBuiltInEntities()
    }
}