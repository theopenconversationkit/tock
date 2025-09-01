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

package ai.tock.bot.connector

import ai.tock.bot.definition.ConnectorSpecificHandling
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS

/**
 * `@ConnectorHandler` is a meta-annotation for annotation classes that are used
 * to provide a [ConnectorSpecificHandling] for a specific connector type.
 *
 * The annotated class must have a single property
 * with the name `value` and the type `KClass<out ConnectorSpecificHandling>`.
 *
 * This annotation is only supposed to be used by connector implementations, not by regular user code.
 */
@Target(ANNOTATION_CLASS)
@MustBeDocumented
annotation class ConnectorHandler(val connectorTypeId: String)
