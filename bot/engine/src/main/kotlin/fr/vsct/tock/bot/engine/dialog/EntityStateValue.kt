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

import fr.vsct.tock.bot.engine.action.Action
import ft.vsct.tock.nlp.api.client.model.EntityValue

/**
 *
 */
data class EntityStateValue(var value: EntityValue,
                            val history: MutableList<ArchivedEntityValue> = mutableListOf()) {

    constructor(action: Action, entityValue: EntityValue)
            : this(entityValue, mutableListOf(ArchivedEntityValue(entityValue, action)))

    data class ArchivedEntityValue(
            val entityValue: EntityValue,
            val action: Action)
}