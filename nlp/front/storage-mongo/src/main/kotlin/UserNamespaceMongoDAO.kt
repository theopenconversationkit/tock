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

package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.service.storage.UserNamespaceDAO
import ai.tock.nlp.front.shared.user.UserNamespace
import ai.tock.nlp.front.shared.user.UserNamespace_.Companion.Current
import ai.tock.nlp.front.shared.user.UserNamespace_.Companion.Login
import ai.tock.nlp.front.shared.user.UserNamespace_.Companion.Namespace
import ai.tock.nlp.front.shared.user.UserNamespace_.Companion.Owner
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import org.bson.conversions.Bson
import org.litote.kmongo.and
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import org.litote.kmongo.setValue
import java.util.regex.Pattern

object UserNamespaceMongoDAO : UserNamespaceDAO {

    private val col: MongoCollection<UserNamespace> by lazy {
        val c = MongoFrontConfiguration.database.getCollection<UserNamespace>()
        c.ensureUniqueIndex(Login, Namespace)
        c.ensureIndex(Login)
        c.ensureIndex(Namespace)
        c
    }

    override fun getNamespaces(user: String): List<UserNamespace> = col.find(Login eq user).toList()

    override fun getUsers(namespace: String): List<UserNamespace> =
        col.find(getCaseInsensitiveBsonFilter(Namespace.name, namespace)).toList()

    override fun saveNamespace(namespace: UserNamespace) {
        col.replaceOne(
            and(Login eq namespace.login, Namespace eq namespace.namespace),
            namespace,
            ReplaceOptions().upsert(true)
        )
    }

    override fun deleteNamespace(user: String, namespace: String) {
        col.deleteOne(and(Login eq user, Namespace eq namespace))
    }

    override fun hasNamespace(user: String, namespace: String): Boolean =
        col.countDocuments(and(Login eq user, Namespace eq namespace)) == 1L

    override fun setCurrentNamespace(user: String, namespace: String) {
        col.updateMany(Login eq user, setValue(Current, false))
        col.updateOne(and(Login eq user, Namespace eq namespace), setValue(Current, true))
    }

    override fun isNamespaceOwner(user: String, namespace: String): Boolean =
        col.countDocuments(and(Login eq user, Namespace eq namespace, Owner eq true)) == 1L

    override fun isExistingNamespace(namespace: String): Boolean {
        return col.countDocuments(getCaseInsensitiveBsonFilter(Namespace.name, namespace)) != 0L
    }

    /**
     * obtain the case-insensitive bson filter
     */
    private fun getCaseInsensitiveBsonFilter(field: String, input: String): Bson {
        val escapedNamespace = Pattern.quote(input)
        return Filters.regex(Namespace.name, "^$escapedNamespace$", "i")
    }
}
