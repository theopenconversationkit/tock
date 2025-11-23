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

package ai.tock.nlp.front.shared.user

/**
 * A user/namespace relation.
 */
data class UserNamespace(
    /**
     * The user login
     */
    val login: String,
    /**
     * The applications namespace
     */
    val namespace: String,
    /**
     * Is the user owner of the namespace ?
     */
    val owner: Boolean = false,
    /**
     * Is it the current namespace for the user ?
     */
    val current: Boolean = false,
)
