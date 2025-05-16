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

package ai.tock.shared

/**
 * The Tock namespace.
 */
const val TOCK_NAMESPACE: String = "tock"

/**
 * The default app namespace.
 */
const val DEFAULT_APP_NAMESPACE = "app"

/**
 * The internal app namespace - var only for tests.
 * Use property "tock_default_namespace" and as default value [DEFAULT_APP_NAMESPACE].
 */
@Volatile
var tockAppDefaultNamespace: String = property("tock_default_namespace", DEFAULT_APP_NAMESPACE)

/**
 * The Tock app namespace.
 */
val defaultNamespace: String get() = tockAppDefaultNamespace

/**
 * Allow access to all namespaces - disabled by default for security reasons.
 */
val allowAccessToAllNamespaces : Boolean = booleanProperty("tock_namespace_open_access", false)
