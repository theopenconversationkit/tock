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

package fr.vsct.tock.shared

import java.util.Collections
import java.util.Enumeration

const val TOCK_NAMESPACE: String = "tock"

val defaultNamespace: String = property("tock_default_namespace", "vsc")

fun String.namespace(): String = namespaceAndName().first
fun String.name(): String = namespaceAndName().second
fun String.namespaceAndName(): Pair<String, String> = this.split(":").let { it[0] to it[1] }

fun String.withNamespace(namespace: String): String = if (contains(":")) this else "$namespace:$this"
fun String.withoutNamespace(namespace: String? = null): String
        = if (contains(":")) namespace().let { if (namespace == null || it == namespace) name() else this }
else this

fun <K, V> mapNotNullValues(vararg pairs: Pair<K, V?>): Map<K, V> = mapOf(*pairs).filterValues { it != null }.mapValues { it.value!! }

fun <T> Enumeration<T>.toSet(): Set<T> = Collections.list(this).toSet()

fun <T> Iterator<T>.toList(): List<T> = mutableListOf<T>().apply {
    while (hasNext()) {
        add(next())
    }
}
