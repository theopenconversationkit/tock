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

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import fr.vsct.tock.shared.cache.TockCache
import fr.vsct.tock.shared.cache.mongo.MongoCache
import fr.vsct.tock.shared.vertx.vertxExecutor

/**
 * internal injector - reset it only for tests.
 */
var tockInternalInjector = KodeinInjector()

/**
 * main Tock injector.
 */
val injector: KodeinInjector get() = tockInternalInjector

/**
 * extension function. Pattern:
 * <code>val core: NlpCore get() = injector.provide()</code>
 */
inline fun <reified T : Any> KodeinInjector.provide(tag: Any? = null): T = injector.provider<T>(tag).value.invoke()

/**
 * IOC of shared module.
 */
val sharedModule = Kodein.Module {
    bind<Executor>() with provider { vertxExecutor() }
    bind<TockCache>() with provider { MongoCache }
}