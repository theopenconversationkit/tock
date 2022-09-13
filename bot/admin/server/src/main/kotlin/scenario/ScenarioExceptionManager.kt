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

import ai.tock.shared.exception.TockException
import ai.tock.shared.exception.rest.ConflictException
import ai.tock.shared.exception.rest.InternalServerException
import ai.tock.shared.exception.rest.NotFoundException
import ai.tock.shared.exception.rest.RestException
import ai.tock.shared.exception.scenario.*
import mu.KLogger

class ScenarioExceptionManager(private val logger: KLogger) {

    fun <O> catch(fallibleSection: () -> O): O {
        try {
            return fallibleSection.invoke()
        } catch(scenarioException : ScenarioException) {
            logger.error(scenarioException.message)
            throw scenarioException.toRestException()
        } catch (tockException: TockException) {
            // TockException use a non-null message,
            // but extends RuntimeException which has nullable message.
            // "tockException.message" cannot be null
            logger.error(tockException.message!!)
            throw InternalServerException(tockException.message!!)
        }
    }

    private fun ScenarioException.toRestException(): RestException {
        return when(this) {
            is BadNumberOfScenarioException -> toRestException()
            is BadNumberException -> toRestException()
            is BadScenarioStateException -> toRestException()
            is BadScenarioVersionException -> toRestException()
            is DuplicateVersionException -> toRestException()
            is MismatchedScenarioException -> toRestException()
            is ScenarioArchivedException -> toRestException()
            is ScenarioEmptyException -> toRestException()
            is ScenarioNotFoundException -> toRestException()
            is ScenarioVersionNotFoundException -> toRestException()
            is ScenarioWithNoIdException -> toRestException()
            is ScenarioWithNoVersionIdException -> toRestException()
            is ScenarioWithVersionException -> toRestException()
            is VersionUnknownException -> toRestException()
            else -> InternalServerException("unmanaged exception") // don't expose the real origin of exception
        }
    }

    private val SAGA_NOT_FOUND = "saga %snot found"
    private val SCENARIO_NOT_FOUND = "scenario %snot found"
    private val SCENARIO_ID_DIFF_FROM_URI = "scenario id in the uri must be the same as in the body but they are different"
    private val SCENARIO_IDS_MISMATCH = "scenario id %sto update must be the same in database but it was %s"
    private val SCENARIO_ARCHIVE = "scenario %sstate in database is 'ARCHIVE', operation forbidden"
    private val BAD_STATE = "scenario state must be %s, but is %s"
    private val SCENARIO_MUST_HAVE_SAGA_ID = "scenario must have saga id but is null"
    private val SCENARIO_MUST_HAVE_ID = "scenario must have id but is null"
    private val DUPLICATE_VERSION = "duplicate scenario %s cannot be updated"
    private val EMPTY_SCENARIO = "scenario %smust be not empty"
    private val SCENARIO_MUST_NOT_HAVE_ID = "scenario must not have id to create but it's %s"
    private val BAD_NUMBER_OF_SAGA = "scenario must appear in %d saga but %d found"
    private val BAD_NUMBER_OF_SCENARIO = "%d scenario expected but %d found"
    private val UNKNOWN_SCENARIO = "cannot update scenario %s that is not in the saga"


    private fun BadNumberOfScenarioException.toRestException(): RestException {
        return InternalServerException(BAD_NUMBER_OF_SAGA.format(expected, received))
    }

    private fun BadNumberException.toRestException(): RestException {
        return InternalServerException(BAD_NUMBER_OF_SCENARIO.format(expected, received))
    }

    private fun BadScenarioStateException.toRestException(): RestException {
        return ConflictException(BAD_STATE.format(excepted.joinToString(" or "), received))
    }

    private fun BadScenarioVersionException.toRestException(): RestException {
        return ConflictException(SCENARIO_ID_DIFF_FROM_URI.format(version))
    }

    private fun DuplicateVersionException.toRestException(): RestException {
        return ConflictException(DUPLICATE_VERSION.format(version))
    }

    private fun MismatchedScenarioException.toRestException(): RestException {
        return ConflictException(SCENARIO_IDS_MISMATCH.format(expected.toDisplay(), received))
    }

    private fun ScenarioArchivedException.toRestException(): RestException {
        return ConflictException(SCENARIO_ARCHIVE.format(version.toDisplay()))
    }

    private fun ScenarioEmptyException.toRestException(): RestException {
        return InternalServerException(EMPTY_SCENARIO.format(id.toDisplay()))
    }

    private fun ScenarioNotFoundException.toRestException(): RestException {
        return NotFoundException(SAGA_NOT_FOUND.format(id.toDisplay()))
    }

    private fun ScenarioVersionNotFoundException.toRestException(): RestException {
        return NotFoundException(SCENARIO_NOT_FOUND.format(id.toDisplay()))
    }

    private fun ScenarioWithNoIdException.toRestException(): RestException {
        return InternalServerException(SCENARIO_MUST_HAVE_SAGA_ID)
    }

    private fun ScenarioWithNoVersionIdException.toRestException(): RestException {
        return InternalServerException(SCENARIO_MUST_HAVE_ID)
    }

    private fun ScenarioWithVersionException.toRestException(): RestException {
        return ConflictException(SCENARIO_MUST_NOT_HAVE_ID.format(id))
    }

    private fun VersionUnknownException.toRestException(): RestException {
        return ConflictException(UNKNOWN_SCENARIO.format(version))
    }

    /**
     * if value not null, add space after it to correctly display in message
     */
    private val toDisplay: String?.() -> String = {
        this?.let { "$this " } ?: ""
    }
}