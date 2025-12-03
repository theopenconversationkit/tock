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

package ai.tock.nlp.front.shared.monitoring

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import org.litote.kmongo.Id
import java.time.Instant
import java.time.Instant.now

/**
 * A user action description - used to trace modifications.
 */
data class UserActionLog(
    /**
     * The namespace of the application.
     */
    val namespace: String,
    /**
     * The application identifier.
     */
    val applicationId: Id<ApplicationDefinition>?,
    /**
     * The user login.
     */
    val login: String,
    /**
     * The action type.
     */
    val actionType: String,
    /**
     * New Data of any.
     */
    val newData: Any?,
    /**
     * Is there a technical error?
     */
    val error: Boolean = false,
    /**
     * Date of the action.
     */
    val date: Instant = now(),
)
