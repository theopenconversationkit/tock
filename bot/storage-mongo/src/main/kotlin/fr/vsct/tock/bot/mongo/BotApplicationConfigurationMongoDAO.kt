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

import com.mongodb.client.model.IndexOptions
import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.admin.bot.BotApplicationConfigurationDAO
import fr.vsct.tock.shared.error
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.litote.kmongo.createIndex
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.replaceOne
import org.litote.kmongo.save

/**
 *
 */
object BotApplicationConfigurationMongoDAO : BotApplicationConfigurationDAO {

    private val logger = KotlinLogging.logger {}

    private val col = MongoBotConfiguration.database.getCollection<BotApplicationConfiguration>("bot_configuration")

    init {
        col.createIndex("{applicationId:1, botId:1}", IndexOptions().unique(true))
    }

    override fun getConfigurationById(id: String): BotApplicationConfiguration? {
        //TODO remove object id hook
        return col.findOneById(id) ?: col.findOneById(ObjectId(id))
    }

    override fun getConfigurationByApplicationIdAndBotId(applicationId: String, botId: String): BotApplicationConfiguration? {
        return col.findOne("{applicationId:${applicationId.json}, botId:${botId.json}}")
    }

    override fun save(conf: BotApplicationConfiguration): BotApplicationConfiguration {
        return try {
            col.save(conf)
            conf
        } catch(e: Exception) {
            //TODO remove object id hook
            logger.error(e)
            val filter = "{applicationId:${conf.applicationId.json}, botId:${conf.botId.json}}"
            col.replaceOne(filter, conf)
            col.findOne(filter)!!
        }
    }

    override fun delete(conf: BotApplicationConfiguration) {
        col.deleteOneById(conf._id!!)
        //TODO remove object id hook
        col.deleteOneById(ObjectId(conf._id!!))
    }

    override fun updateIfNotManuallyModified(conf: BotApplicationConfiguration): BotApplicationConfiguration {
        return col.findOne("{applicationId:${conf.applicationId.json}, botId:${conf.botId.json}}").let {
            if (it == null) {
                col.save(conf)
                conf
            } else {
                if (!it.manuallyModified) {
                    conf.copy(_id = it._id).apply { save(this) }
                } else {
                    it
                }
            }
        }
    }

    override fun getConfigurationsByNamespaceAndNlpModel(namespace: String, nlpModel: String): List<BotApplicationConfiguration> {
        return col.find("{namespace:${namespace.json}, nlpModel:${nlpModel.json}}").toList()
    }

    override fun getConfigurations(): List<BotApplicationConfiguration> {
        return col.find().toList()
    }
}