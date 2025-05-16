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

package ai.tock.nlp.front.shared.build

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import org.litote.kmongo.Id

/**
 * Trigger NLP model build.
 */
data class ModelBuildTrigger(
    /**
     * The application id to build
     */
    val applicationId: Id<ApplicationDefinition>,
    /**
     * Is it a full rebuild?
     */
    val all: Boolean,
    /**
     * Builds only if model does not exist
     */
    val onlyIfModelNotExists: Boolean = false
)
