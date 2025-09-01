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

package ai.tock.shared.coroutines

import kotlin.RequiresOptIn.Level.WARNING

/**
 * The experimental TOCK Coroutine API marker.
 *
 * *Implementation note:* TOCK coroutines are executed by default in a Vert.x Worker thread.
 *
 * Any usage of a declaration annotated with `@ExperimentalTockCoroutines` must be accepted either by
 * annotating that usage with the [OptIn] annotation, e.g. `@OptIn(ExperimentalTockCoroutines::class)`,
 * or by using the compiler argument `-opt-in=ai.tock.bot.engine.ExperimentalTockCoroutines`.
 */
@RequiresOptIn(level = WARNING)
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalTockCoroutines
