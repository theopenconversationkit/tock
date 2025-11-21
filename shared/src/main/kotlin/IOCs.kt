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

import ai.tock.shared.cache.TockCache
import ai.tock.shared.cache.mongo.MongoCache
import ai.tock.shared.security.NoOpTockUserListener
import ai.tock.shared.security.TockUserListener
import ai.tock.shared.security.auth.spi.WebSecurityHandler
import ai.tock.shared.security.auth.spi.WebSecurityMode
import ai.tock.shared.security.mongo.DefaultMongoCredentialsProvider
import ai.tock.shared.security.mongo.MongoCredentialsProvider
import ai.tock.shared.vertx.TockVertxProvider
import ai.tock.shared.vertx.VertxProvider
import ai.tock.shared.vertx.WebSecurityCookiesHandler
import ai.tock.shared.vertx.WebSecurityPassthroughHandler
import ai.tock.shared.vertx.vertxExecutor
import com.github.salomonbrys.kodein.*
import com.mongodb.client.MongoClient
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

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
inline fun <reified T : Any> KodeinInjector.provide(tag: Any? = null): T =
    injector.provider<T>(tag).value.invoke()

/**
 * Extension function for Ioc. Pattern:
 * <code>val core: NlpCore get() = injector.provideOrDefault() { ... }</code>
 */
inline fun <reified T : Any> KodeinInjector.provideOrDefault(tag: Any? = null, defaultValueProvider: () -> T): T =
    try {
        injector.providerOrNull<T>(tag).value?.invoke() ?: defaultValueProvider.invoke()
    } catch (e: KodeinInjector.UninjectedException) {
        defaultValueProvider.invoke()
    }

/**
 * IOC of shared module.
 */
val sharedModule = Kodein.Module {
    bind<Executor>() with provider { vertxExecutor() }
    bind<TockCache>() with provider { MongoCache }
    bind<VertxProvider>() with provider { TockVertxProvider }
    bind<TockUserListener>() with provider { NoOpTockUserListener }
    bind<MongoCredentialsProvider>() with provider { DefaultMongoCredentialsProvider }
    bind<WebSecurityHandler>(tag = WebSecurityMode.COOKIES.name) with singleton { WebSecurityCookiesHandler() }
    bind<WebSecurityHandler>(tag = WebSecurityMode.PASSTHROUGH.name) with singleton { WebSecurityPassthroughHandler() }

    try {
        bind<MongoClient>() with singleton { mongoClient }
    } catch (e: Exception) {
        logger.warn { e.message }
    }
    try {
        bind<com.mongodb.reactivestreams.client.MongoClient>() with singleton { asyncMongoClient }
    } catch (e: Exception) {
        logger.warn { e.message }
    }
}
