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

package ai.tock.translator

import java.util.Locale

/**
 * Contains all data used to select the more relevant translation.
 */
data class I18nContext(
    /**
     * The current user [Locale].
     */
    val userLocale: Locale,
    /**
     * The current user interface type.
     */
    val userInterfaceType: UserInterfaceType,
    /**
     * The connector identifier used for the response.
     */
    val connectorId: String? = null,
    /**
     * The current context identifier.
     */
    val contextId: String? = null,
)
