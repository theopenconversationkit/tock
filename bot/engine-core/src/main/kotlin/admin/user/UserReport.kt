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

package ai.tock.bot.admin.user

import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.bot.engine.user.UserState
import java.time.Instant
import java.time.Instant.now

/**
 *
 */
data class UserReport(
    val playerId: PlayerId,
    val applicationIds: MutableSet<String>,
    val userPreferences: UserPreferences = UserPreferences(),
    val userState: UserState = UserState(),
    val lastUpdateDate: Instant = now(),
    val lastActionText: String? = null,
    val lastUserActionDate: Instant,
)
