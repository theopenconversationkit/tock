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

package ai.tock.nlp.front.service.storage

import ai.tock.nlp.front.shared.user.UserNamespace

interface UserNamespaceDAO {

    /**
     * Returns all the namespaces of a user.
     */
    fun getNamespaces(user: String): List<UserNamespace>

    /**
     * Returns all the users of a namespace.
     */
    fun getUsers(namespace: String): List<UserNamespace>

    /**
     * Persists namespace.
     */
    fun saveNamespace(namespace: UserNamespace)

    /**
     * Delete namespace.
     */
    fun deleteNamespace(user: String, namespace: String)

    /**
     * Set current namespace for selected user.
     */
    fun setCurrentNamespace(user: String, namespace: String)

    /**
     * Is it the namespace owner ?
     */
    fun isNamespaceOwner(user: String, namespace: String): Boolean

    /**
     * Is this user has the namespace ?
     */
    fun hasNamespace(user: String, namespace: String): Boolean

    /**
     * Is this namespace exists ?
     */
    fun isExistingNamespace(namespace: String): Boolean
}
