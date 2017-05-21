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
import fr.vsct.tock.nlp.model.ClassifierContextKey
import fr.vsct.tock.nlp.model.EntityContextKey
import fr.vsct.tock.nlp.model.IntentContext.IntentContextKey
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

    private fun getGridFSFile(bucket: GridFSBucket, key: ClassifierContextKey): GridFSFile? {
        return try {
            bucket.find(Filters.eq("metadata", key)).firstOrNull()
        } catch(e: MongoGridFSException) {
            logger.debug(e) { "no model exists for $key" }
            null
        }
    }

    private fun getDownloadStream(bucket: GridFSBucket, key: ClassifierContextKey): GridFSDownloadStream? {
        return getGridFSFile(bucket, key)?.let {
            bucket.openDownloadStream(it.id)
        }
    }

    private fun saveModel(bucket: GridFSBucket, key: ClassifierContextKey, stream: InputStream) {
        val newId = bucket.uploadFromStream(key.name(), stream, GridFSUploadOptions().metadata(Document.parse(key.json)))
        //remove old versions
        bucket.find(Filters.eq("metadata", key)).forEach {
            if (it.objectId != newId) {
                logger.debug { "Remove file ${it.objectId} for $key" }
                bucket.delete(it.objectId)
            }
        }
    }

    private fun getModelInputStream(bucket: GridFSBucket, key: ClassifierContextKey): NlpModelStream? {
        return getDownloadStream(bucket, key)?.let {
            NlpModelStream(it, it.gridFSFile.uploadDate.toInstant())
        }
    }

    private fun getLastUpdate(bucket: GridFSBucket, key: ClassifierContextKey): Instant? {
        return getGridFSFile(bucket, key)?.uploadDate?.toInstant()
    }

    override fun getEntityModelInputStream(key: EntityContextKey): NlpModelStream? {
        return getModelInputStream(entityBucket, key)
    }

    override fun saveEntityModel(key: EntityContextKey, stream: InputStream) {
        saveModel(entityBucket, key, stream)
    }

    override fun getEntityModelLastUpdate(key: EntityContextKey): Instant? {
        return getLastUpdate(entityBucket, key)
    }

    override fun getIntentModelInputStream(key: IntentContextKey): NlpModelStream? {
        return getModelInputStream(intentBucket, key)
    }

    override fun saveIntentModel(key: IntentContextKey, stream: InputStream) {
        saveModel(intentBucket, key, stream)
    }

    override fun getIntentModelLastUpdate(key: IntentContextKey): Instant? {
        return getLastUpdate(intentBucket, key)
    }
}