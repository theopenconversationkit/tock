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

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.provider
import com.github.salomonbrys.kodein.providerOrNull
import java.util.ServiceLoader

/**
 * Internal injector - reset only for tests.
 */
var tockInternalInjector = KodeinInjector()

/**
 * Main Tock injector.
 */
val injector: KodeinInjector get() = tockInternalInjector

/**
 * Extension function for Ioc. Pattern:
 * <code>val core: NlpCore get() = injector.provide()</code>
 */
inline fun <reified T : Any> KodeinInjector.provide(tag: Any? = null): T = injector.provider<T>(tag).value.invoke()

/**
 * Extension function for Ioc. Pattern:
 * <code>val core: NlpCore get() = injector.provideOrDefault() { ... }</code>
 */
inline fun <reified T : Any> KodeinInjector.provideOrDefault(
    tag: Any? = null,
    defaultValueProvider: () -> T,
): T =
    try {
        injector.providerOrNull<T>(tag).value?.invoke() ?: defaultValueProvider.invoke()
    } catch (_: KodeinInjector.UninjectedException) {
        defaultValueProvider.invoke()
    }

/**
 * Adds bindings to [sharedModule]. The implementations are found with the [ServiceLoader]:
 * tock-shared adds the Vert.x and MongoDB ones, so that tock-shared-core depends on neither.
 */
@InternalTockApi
interface SharedModuleContributor {
    fun Kodein.Builder.contribute()
}

/**
 * IOC of shared module.
 */
@OptIn(InternalTockApi::class)
val sharedModule =
    Kodein.Module {
        ServiceLoader.load(SharedModuleContributor::class.java).forEach { contributor ->
            with(contributor) { contribute() }
        }
    }
