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

import ai.tock.nlp.front.service.storage.NamespaceConfigurationDAO
import ai.tock.nlp.front.shared.namespace.NamespaceConfiguration
import ai.tock.nlp.front.shared.namespace.NamespaceConfiguration_.Companion.DefaultSharingConfiguration
import ai.tock.nlp.front.shared.namespace.NamespaceConfiguration_.Companion.Namespace
import ai.tock.nlp.front.shared.namespace.NamespaceSharingConfiguration
import ai.tock.shared.watch
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.ReplaceOptions
import org.litote.kmongo.div
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.or
import org.litote.kmongo.reactivestreams.getCollection

object NamespaceConfigurationMongoDAO : NamespaceConfigurationDAO {
    private val col: MongoCollection<NamespaceConfiguration> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<NamespaceConfiguration>()
        c.ensureUniqueIndex(Namespace)
        c.ensureIndex(DefaultSharingConfiguration / NamespaceSharingConfiguration::model)
        c.ensureIndex(DefaultSharingConfiguration / NamespaceSharingConfiguration::stories)
        c
    }

    private val asyncCol by lazy {
        MongoFrontConfiguration.asyncDatabase.getCollection<NamespaceConfiguration>()
    }

    override fun saveNamespaceConfiguration(configuration: NamespaceConfiguration) {
        col.replaceOne(Namespace eq configuration.namespace, configuration, ReplaceOptions().upsert(true))
    }

    override fun getNamespaceConfiguration(namespace: String): NamespaceConfiguration? = col.findOne(Namespace eq namespace)

    override fun getSharableNamespaceConfiguration(): List<NamespaceConfiguration> =
        col.find(
            or(
                DefaultSharingConfiguration / NamespaceSharingConfiguration::model eq true,
                DefaultSharingConfiguration / NamespaceSharingConfiguration::stories eq true,
            ),
        )
            .toList()

    override fun listenNamespaceConfigurationChanges(listener: () -> Unit) {
        asyncCol.watch { listener() }
    }
}
