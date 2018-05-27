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

package fr.vsct.tock.bot.engine.dialog

import fr.vsct.tock.nlp.api.client.model.NlpEntity
import java.time.Instant
import java.time.Instant.now

/**
 * A "snapshot" is a readonly view of the state in the dialog, usually after a bot reply.
 */
data class Snapshot(
    val intentName: String?,
    val entityValues: List<EntityValue>,
    val date: Instant = now()) {

    /**
     * Does this value exist in the snapshot?
     */
    fun hasValue(entity: NlpEntity): Boolean = getValue(entity) != null

    /**
     * Returns the value if it exists.
     */
    fun getValue(entity: NlpEntity): EntityValue? = entityValues.firstOrNull { it.entity == entity }
}