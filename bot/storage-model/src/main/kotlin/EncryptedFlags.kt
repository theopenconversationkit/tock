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

package ai.tock.bot.mongo

import ai.tock.shared.InternalTockApi
import ai.tock.shared.listProperty

/**
 * The flags of the user timelines that are stored encrypted (`tock_bot_encrypted_flags`).
 */
@InternalTockApi
object EncryptedFlags {
    private val encryptedFlags = listProperty("tock_bot_encrypted_flags", emptyList()).toSet()

    fun hasToEncryptFlag(flag: String): Boolean = encryptedFlags.contains(flag)
}
