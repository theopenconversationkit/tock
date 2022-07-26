/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.admin.scenario

import ai.tock.shared.exception.rest.ConflictException
import ai.tock.shared.exception.rest.InternalServerException
import ai.tock.shared.exception.rest.NotFoundException

/**
 * Throws RestException if scenario cannot be created in database
 */
val checkToCreate: Scenario.() -> Scenario = {
    if(id != null && id!!.isNotBlank()) {
        throw ConflictException("scenario id must be null, but is $id")
    } else {
        this
    }
}

/**
 * Throws RestException if scenario cannot be created in database
 */
val checkToUpdate: Scenario.(String) -> Scenario = { scenarioId ->
    if(scenarioId != id) {
        throw ConflictException("the scenario id of the uri must be the same as in the body but they are different, $scenarioId ≠ $id")
    } else {
        this
    }
}

/**
 * Check if the Scenario is not null, and return it
 * else throws RestException scenario not found
 */
val checkIsNotNullForId: Scenario?.(String?) -> Scenario = { id ->
    if(this == null) {
        //if id not null, add space after id to correctly display id in exception
        val displayId: String = id?.let { "$id " } ?: ""
        throw NotFoundException("scenario {$displayId}not found")
    } else {
        this
    }
}

/**
 * Throws RestException if scenario does not exist in database
 * @properties scenario from database (null if does not exist)
 */
val mustExist: Scenario.(Scenario?) -> Scenario = { exist ->
    if(exist == null) {
        throw NotFoundException("scenario id ${this.id} not found")
    } else {
        this
    }
}

/*
 * Throws RestException if id is null
 */
val checkScenarioFromDatabase: Scenario.() -> Scenario = {
    if (this.id == null) {
        throw InternalServerException("scenario id from database cannot be null")
    } else {
        this
    }
}