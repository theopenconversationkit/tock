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

package ai.tock.shared.security

/**
 * Tock user admin listener.
 */
interface TockUserListener {
    /**
     * Try to register a tock user, and returns the registered user.
     *
     * @param user the user
     * @param joinNamespace should we create a non existing namespace, or join the specified user namespace?
     */
    fun registerUser(
        user: TockUser,
        joinNamespace: Boolean = false,
    ): TockUser
}
