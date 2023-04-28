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

package ai.tock.bot.admin.mapper

import ai.tock.shared.exception.rest.ConflictException
import ai.tock.shared.exception.rest.NotFoundException
import ai.tock.shared.exception.rest.RestException
import ai.tock.shared.exception.scenario.ScenarioException
import ai.tock.shared.exception.scenario.group.ScenarioGroupAndVersionMismatchException
import ai.tock.shared.exception.scenario.group.ScenarioGroupDuplicatedException
import ai.tock.shared.exception.scenario.group.ScenarioGroupInvalidException
import ai.tock.shared.exception.scenario.group.ScenarioGroupNotFoundException
import ai.tock.shared.exception.scenario.group.ScenarioGroupWithVersionsException
import ai.tock.shared.exception.scenario.group.ScenarioGroupWithoutVersionException
import ai.tock.shared.exception.scenario.version.ScenarioVersionBadStateException
import ai.tock.shared.exception.scenario.version.ScenarioVersionDuplicatedException
import ai.tock.shared.exception.scenario.version.ScenarioVersionNotFoundException
import ai.tock.shared.exception.scenario.version.ScenarioVersionsInconsistentException
import mu.KLogger
import mu.KotlinLogging

object ScenarioExceptionManager {

    private val logger: KLogger = KotlinLogging.logger {}

    fun <O> catch(fallibleSection: () -> O): O {
        try {
            return fallibleSection.invoke()
        } catch(ex : ScenarioException) {
            logger.error(ex.message)
            throw ex.toRestException()
        }
    }

    private fun ScenarioException.toRestException(): RestException {
        return when(this) {
            is ScenarioGroupAndVersionMismatchException -> ConflictException(message)
            is ScenarioGroupDuplicatedException -> ConflictException(message)
            is ScenarioGroupNotFoundException -> NotFoundException(message)
            is ScenarioGroupWithoutVersionException -> ConflictException(message)
            is ScenarioGroupWithVersionsException -> ConflictException(message)
            is ScenarioVersionBadStateException -> ConflictException(message)
            is ScenarioVersionDuplicatedException -> ConflictException(message)
            is ScenarioVersionNotFoundException -> NotFoundException(message)
            is ScenarioVersionsInconsistentException -> ConflictException(message)
            is ScenarioGroupInvalidException -> ConflictException(message)
            else -> RestException()
        }
    }

}