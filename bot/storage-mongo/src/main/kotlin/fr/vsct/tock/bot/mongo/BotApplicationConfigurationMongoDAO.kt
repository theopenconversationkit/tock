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
import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration_.Companion.Namespace
import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration_.Companion.NlpModel
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.watchSafely
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.async.getCollectionOfName
import org.litote.kmongo.deleteOne
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.save

/**
 *
 */
internal object BotApplicationConfigurationMongoDAO : BotApplicationConfigurationDAO {

    private val logger = KotlinLogging.logger {}

    private val col = MongoBotConfiguration.database.getCollection<BotApplicationConfiguration>("bot_configuration")
    private val asyncCol =
        MongoBotConfiguration.asyncDatabase.getCollectionOfName<BotApplicationConfiguration>("bot_configuration")


    init {
        col.ensureUniqueIndex(ApplicationId, BotId)
    }

    override fun listenChanges(listener: () -> Unit) {
        asyncCol.watchSafely { listener() }
    }

    override fun getConfigurationById(id: Id<BotApplicationConfiguration>): BotApplicationConfiguration? {
        //TODO remove object id hook
        return col.findOneById(id) ?: col.findOneById(ObjectId(id.toString()))
    }

    override fun getConfigurationByApplicationIdAndBotId(
        applicationId: String,
        botId: String
    ): BotApplicationConfiguration? {
        return col.findOne(ApplicationId eq applicationId, BotId eq botId)
    }

    override fun getConfigurationsByBotId(botId: String): List<BotApplicationConfiguration> {
        return col.find(BotId eq botId).toList()
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

    override fun getConfigurations(): List<BotApplicationConfiguration> {
        return col.find().toList()
    }
}