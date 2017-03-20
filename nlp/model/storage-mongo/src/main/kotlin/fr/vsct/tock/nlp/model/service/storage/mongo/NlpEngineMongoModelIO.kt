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

package fr.vsct.tock.nlp.model.service.storage.mongo

import com.github.salomonbrys.kodein.instance
import com.mongodb.MongoGridFSException
import com.mongodb.client.MongoDatabase
import com.mongodb.client.gridfs.GridFSBucket
import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.gridfs.GridFSDownloadStream
import com.mongodb.client.gridfs.model.GridFSFile
import com.mongodb.client.gridfs.model.GridFSUploadOptions
import com.mongodb.client.model.Filters
import fr.vsct.tock.nlp.model.ClassifierContext
import fr.vsct.tock.nlp.model.ClassifierContextKey
import fr.vsct.tock.nlp.model.EntityContext
import fr.vsct.tock.nlp.model.IntentContext
import fr.vsct.tock.nlp.model.service.storage.NlpEngineModelIO
import fr.vsct.tock.nlp.model.service.storage.NlpModelStream
import fr.vsct.tock.shared.injector
import mu.KotlinLogging
import org.bson.Document
import org.litote.kmongo.json
import java.io.InputStream
import java.time.Instant

/**
 *
 */
object NlpEngineMongoModelIO : NlpEngineModelIO {

    private val logger = KotlinLogging.logger {}

    private val database: MongoDatabase by injector.instance(MONGO_DATABASE)
    private val entityBucket: GridFSBucket  by lazy { GridFSBuckets.create(database, "fs_entity") }
    private val intentBucket: GridFSBucket  by lazy { GridFSBuckets.create(database, "fs_intent") }

    private fun <T : ClassifierContextKey> getGridFSFile(bucket: GridFSBucket, context: ClassifierContext<T>): GridFSFile? {
        return context.key().let { key ->
            try {
                bucket.find(Filters.eq("metadata", key)).firstOrNull()
            } catch(e: MongoGridFSException) {
                logger.debug(e) { "no model exists for $context" }
                null
            }
        }
    }

    private fun <T : ClassifierContextKey> getDownloadStream(bucket: GridFSBucket, context: ClassifierContext<T>): GridFSDownloadStream? {
        return getGridFSFile(bucket, context)?.let {
            bucket.openDownloadStream(it.id)
        }
    }

    private fun <T : ClassifierContextKey> saveModel(bucket: GridFSBucket, context: ClassifierContext<T>, stream: InputStream) {
        context.key().let { key ->
            val newId = bucket.uploadFromStream(key.name(), stream, GridFSUploadOptions().metadata(Document.parse(key.json)))
            //remove old versions
            bucket.find(Filters.eq("metadata", key)).forEach {
                if (it.objectId != newId) {
                    logger.debug { "Remove file ${it.objectId} for $context" }
                    bucket.delete(it.objectId)
                }
            }
        }
    }

    private fun <T : ClassifierContextKey> getModelInputStream(bucket: GridFSBucket, context: ClassifierContext<T>): NlpModelStream? {
        return getDownloadStream(bucket, context)?.let {
            NlpModelStream(it, it.gridFSFile.uploadDate.toInstant())
        }
    }

    private fun <T : ClassifierContextKey> getLastUpdate(bucket: GridFSBucket, context: ClassifierContext<T>): Instant? {
        return getGridFSFile(bucket, context)?.uploadDate?.toInstant()
    }

    override fun getEntityModelInputStream(entityContext: EntityContext): NlpModelStream? {
        return getModelInputStream(entityBucket, entityContext)
    }

    override fun saveEntityModel(entityContext: EntityContext, stream: InputStream) {
        saveModel(entityBucket, entityContext, stream)
    }

    override fun getEntityModelLastUpdate(entityContext: EntityContext): Instant? {
        return getLastUpdate(entityBucket, entityContext)
    }

    override fun getIntentModelInputStream(intentContext: IntentContext): NlpModelStream? {
        return getModelInputStream(intentBucket, intentContext)
    }

    override fun saveIntentModel(intentContext: IntentContext, stream: InputStream) {
        saveModel(intentBucket, intentContext, stream)
    }

    override fun getIntentModelLastUpdate(intentContext: IntentContext): Instant? {
        return getLastUpdate(intentBucket, intentContext)
    }
}