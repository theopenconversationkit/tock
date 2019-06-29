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

package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.admin.bot.BotApplicationConfigurationDAO
import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration_.Companion.ApplicationId
import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration_.Companion.BotId
import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration_.Companion.Name
import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration_.Companion.Namespace
import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration_.Companion.NlpModel
import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration_.Companion.Parameters
import fr.vsct.tock.bot.admin.bot.BotConfiguration
import fr.vsct.tock.bot.mongo.MongoBotConfiguration.asyncDatabase
import fr.vsct.tock.bot.mongo.MongoBotConfiguration.database
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.watch
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.deleteOne
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.reactivestreams.getCollectionOfName
import org.litote.kmongo.save
import org.litote.kmongo.upsert

/**
 *
 */
internal object BotApplicationConfigurationMongoDAO : BotApplicationConfigurationDAO {

    private val logger = KotlinLogging.logger {}

    private val botCol = database.getCollection<BotConfiguration>("bot")
    private val asyncBotCol = asyncDatabase.getCollectionOfName<BotConfiguration>("bot")

    private val col = database.getCollection<BotApplicationConfiguration>("bot_configuration")
    private val asyncCol = asyncDatabase.getCollectionOfName<BotApplicationConfiguration>("bot_configuration")

    init {
        col.ensureUniqueIndex(ApplicationId, BotId, Namespace)
        col.ensureIndex(ApplicationId, BotId)
        col.ensureIndex(Namespace, BotId)
        botCol.ensureUniqueIndex(Name, BotId, Namespace)
    }

    override fun listenBotChanges(listener: () -> Unit) {
        asyncBotCol.watch { listener() }
    }

    override fun listenChanges(listener: () -> Unit) {
        asyncCol.watch { listener() }
    }

    override fun getConfigurationById(id: Id<BotApplicationConfiguration>): BotApplicationConfiguration? {
        //TODO remove object id hook
        return col.findOneById(id) ?: col.findOneById(ObjectId(id.toString()))
    }

    override fun getConfigurationByApplicationIdAndBotId(
        namespace: String,
        applicationId: String,
        botId: String
    ): BotApplicationConfiguration? {
        return col.findOne(Namespace eq namespace, ApplicationId eq applicationId, BotId eq botId)
    }

    //TODO remove this in 19.9
    fun getHackedConfigurationByApplicationIdAndBot(
        namespace: String,
        applicationId: String,
        botId: String
    ): BotApplicationConfiguration? {
        return getConfigurationByApplicationIdAndBotId(namespace, applicationId, botId)
            ?: col.findOne(
                Namespace eq namespace,
                Parameters.keyProjection("appId") eq applicationId,
                BotId eq botId
            )
    }

    override fun getConfigurationsByNamespaceAndBotId(namespace: String, botId: String): List<BotApplicationConfiguration> {
        return col.find(Namespace eq namespace, BotId eq botId).toList()
    }

    override fun save(conf: BotApplicationConfiguration): BotApplicationConfiguration {
        return try {
            col.save(conf)
            conf
        } catch (e: Exception) {
            //TODO remove object id hook
            logger.error(e)
            val filter = "{applicationId:${conf.applicationId.json}, botId:${conf.botId.json}}"
            col.deleteOne(filter)
            col.insertOne(conf)
            col.findOne(filter)!!
        }
    }

    override fun delete(conf: BotApplicationConfiguration) {
        col.deleteOneById(conf._id)
        //TODO remove object id hook
        col.deleteOneById(ObjectId(conf._id.toString()))
    }

    override fun getConfigurationsByNamespaceAndNlpModel(
        namespace: String,
        nlpModel: String
    ): List<BotApplicationConfiguration> {
        return col.find(Namespace eq namespace, NlpModel eq nlpModel).toList()
    }

    override fun getConfigurationsByNamespaceAndConfigurationName(namespace: String, configurationName: String): List<BotApplicationConfiguration> {
        return col.find(Namespace eq namespace, Name eq configurationName).toList()
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
            upsert()
        )
    }

    override fun getBotConfigurationsByNamespaceAndBotId(namespace: String, botId: String): List<BotConfiguration> {
        return botCol.find(Namespace eq namespace, BotId eq botId).toList()
    }

    override fun getBotConfigurationsByNamespaceAndNameAndBotId(namespace: String, name: String, botId: String): BotConfiguration? {
        return botCol.findOne(Namespace eq namespace, Name eq name, BotId eq botId)
    }

    override fun getBotConfigurations(): List<BotConfiguration> {
        return botCol.find().toList()
    }

    fun getApplicationIds(namespace: String, nlpModel: String): Set<String> =
        getConfigurationsByNamespaceAndNlpModel(namespace, nlpModel)
            .asSequence()
            .flatMap {
                sequenceOf(
                    it.applicationId,
                    //special messenger connector fix TODO remove this in 19.9
                    it.parameters["pageId"],
                    it.parameters["appId"]
                ).filterNotNull()
            }
            .toSet()
}