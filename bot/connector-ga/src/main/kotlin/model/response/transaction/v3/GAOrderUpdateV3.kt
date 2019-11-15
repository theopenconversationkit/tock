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

package ai.tock.bot.connector.ga.model.response.transaction.v3

import ai.tock.bot.connector.ga.model.response.GAUserNotification

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#orderupdate
 */
data class GAOrderUpdateV3(
        val type: String? = null,
        val updateMask: String? = null,
        val order: GAOrder,
        val userNotification: GAUserNotification? = null,
        val reason: String? = null
)

