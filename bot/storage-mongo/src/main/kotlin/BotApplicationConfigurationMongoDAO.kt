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

package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.bot.BotApplicationConfiguration_.Companion.ApplicationId
import ai.tock.bot.admin.bot.BotApplicationConfiguration_.Companion.BotId
import ai.tock.bot.admin.bot.BotApplicationConfiguration_.Companion.Name
import ai.tock.bot.admin.bot.BotApplicationConfiguration_.Companion.Namespace
import ai.tock.bot.admin.bot.BotApplicationConfiguration_.Companion.NlpModel
import ai.tock.bot.admin.bot.BotApplicationConfiguration_.Companion.Path
import ai.tock.bot.admin.bot.BotApplicationConfiguration_.Companion.TargetConfigurationId
import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.mongo.MongoBotConfiguration.asyncDatabase
import ai.tock.bot.mongo.MongoBotConfiguration.database
import ai.tock.shared.ensureIndex
import ai.tock.shared.ensureUniqueIndex
import ai.tock.shared.error
import ai.tock.shared.watch
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.reactivestreams.getCollectionOfName
import org.litote.kmongo.replaceUpsert
import org.litote.kmongo.save

/**
 *
 */
internal object BotApplicationConfigurationMongoDAO : BotApplicationConfigurationDAO {

    private val logger = KotlinLogging.logger {}

    private val botCol = database.getCollection<BotConfiguration>("bot")
    private val asyncBotCol = asyncDatabase.getCollectionOfName<BotConfiguration>("bot")

    val col = database.getCollection<BotApplicationConfiguration>("bot_configuration")
    private val asyncCol = asyncDatabase.getCollectionOfName<BotApplicationConfiguration>("bot_configuration")

    init {
        try {
            col.ensureUniqueIndex(ApplicationId, BotId, Namespace)
            col.ensureUniqueIndex(Path)
            col.ensureIndex(ApplicationId, BotId)
            col.ensureIndex(Namespace, BotId)
            botCol.ensureUniqueIndex(Name, BotId, Namespace)
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    override fun listenBotChanges(listener: () -> Unit) {
        asyncBotCol.watch { listener() }
    }

    override fun listenChanges(listener: () -> Unit) {
        asyncCol.watch { listener() }
    }

    override fun getConfigurationById(id: Id<BotApplicationConfiguration>): BotApplicationConfiguration? {
        return col.findOneById(id)
    }

    override fun getConfigurationByApplicationIdAndBotId(
        namespace: String,
        applicationId: String,
        botId: String
    ): BotApplicationConfiguration? {
        return col.findOne(Namespace eq namespace, ApplicationId eq applicationId, BotId eq botId)
    }

    override fun getConfigurationsByNamespaceAndBotId(
        namespace: String,
        botId: String
    ): List<BotApplicationConfiguration> {
        return col.find(Namespace eq namespace, BotId eq botId).toList()
    }

    override fun getConfigurationByPath(path: String): BotApplicationConfiguration? {
        return col.findOne(Path eq path)
    }

    override fun getConfigurationByTargetId(id: Id<BotApplicationConfiguration>): BotApplicationConfiguration? {
        return col.findOne(TargetConfigurationId eq id)
    }

    override fun save(conf: BotApplicationConfiguration): BotApplicationConfiguration {
        return try {
            col.save(conf)
            conf
        } catch (e: Exception) {
            logger.error(e)
            conf
        }
    }

    override fun delete(conf: BotApplicationConfiguration) {
        col.deleteOneById(conf._id)
    }

    override fun getConfigurationsByNamespaceAndNlpModel(
        namespace: String,
        nlpModel: String
    ): List<BotApplicationConfiguration> {
        return col.find(Namespace eq namespace, NlpModel eq nlpModel).toList()
    }

    override fun getConfigurationsByBotNamespaceAndConfigurationName(
        namespace: String,
        botId: String,
        configurationName: String
    ): List<BotApplicationConfiguration> {
        return col.find(Namespace eq namespace, BotId eq botId, Name eq configurationName).toList()
    }

    override fun getConfigurations(): List<BotApplicationConfiguration> {
        return col.find().toList()
    }

    override fun save(conf: BotConfiguration) {
        botCol.replaceOne(
            and(
                Name eq conf.name,
                Namespace eq conf.namespace,
                BotId eq conf.botId
            ),
            conf,
            replaceUpsert()
        )
    }

    override fun getBotConfigurationsByNamespaceAndBotId(namespace: String, botId: String): List<BotConfiguration> {
        return botCol.find(Namespace eq namespace, BotId eq botId).toList()
    }

    override fun getBotConfigurationsByNamespaceAndNameAndBotId(
        namespace: String,
        name: String,
        botId: String
    ): BotConfiguration? {
        return botCol.findOne(Namespace eq namespace, Name eq name, BotId eq botId)
    }

    override fun getBotConfigurations(): List<BotConfiguration> {
        return botCol.find().toList()
    }

    override fun delete(conf: BotConfiguration) {
        botCol.deleteOne(
            and(
                Name eq conf.name,
                Namespace eq conf.namespace,
                BotId eq conf.botId
            )
        )
    }

    fun getApplicationIds(namespace: String, nlpModel: String): Set<String> =
        getConfigurationsByNamespaceAndNlpModel(namespace, nlpModel)
            .asSequence()
            .flatMap {
                sequenceOf(
                    it.applicationId,
                    it._id.toString(),
                )
            }
            .toSet()
}
