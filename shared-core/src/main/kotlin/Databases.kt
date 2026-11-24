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

package ai.tock.shared

/**
 * Tock bot database name property
 */
const val TOCK_BOT_DATABASE: String = "tock_bot_mongo_db"

/**
 * Tock front database name property
 */
const val TOCK_FRONT_DATABASE: String = "tock_front_mongo_db"

/**
 * Tock model database name property
 */
const val TOCK_MODEL_DATABASE: String = "tock_model_mongo_db"

/**
 * Tock cache database name property
 */
const val TOCK_CACHE_DATABASE: String = "tock_cache_mongo_db"

private val documentDbOn = booleanProperty("tock_document_db_on", false)

/**
 * Replaces the dots of the keys of [data] (maps and lists, recursively) when the MongoDB storage is AWS DocumentDB
 * (`tock_document_db_on`), which does not support dots in a field name. The documents of the storages use it:
 * they are also stored without MongoDB, by the serverless runtime.
 */
fun transformDocumentData(data: Any?): Any? =
    if (documentDbOn) {
        data?.let {
            when (it) {
                is Map<*, *> -> it.mapKeys { (key, _) -> key.toString().replace(".", "_DOT_") }.mapValues { (_, value) -> transformDocumentData(value) }
                is List<*> -> it.map { elem -> transformDocumentData(elem) }
                else -> it
            }
        }
    } else {
        data
    }
