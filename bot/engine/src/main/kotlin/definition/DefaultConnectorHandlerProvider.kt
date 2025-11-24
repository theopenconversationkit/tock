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

package ai.tock.bot.definition

import ai.tock.bot.connector.ConnectorHandler
import ai.tock.bot.connector.ConnectorIdHandlers
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.BotBus
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import ai.tock.shared.mapNotNullValues
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses
import kotlin.reflect.typeOf

internal object DefaultConnectorHandlerProvider : ConnectorHandlerProvider {
    private val connectorHandlerMap: MutableMap<KClass<*>, Map<String, KClass<*>>> = ConcurrentHashMap()

    private val connectorIdHandlerMap: MutableMap<KClass<*>, Map<String, KClass<*>>> = ConcurrentHashMap()

    private fun getConnectorHandlerMap(contextClass: KClass<*>): Map<String, KClass<*>> {
        return connectorHandlerMap.getOrPut(contextClass) {
            getAllAnnotations(contextClass)
                .filter { it.annotationClass.findAnnotation<ConnectorHandler>() != null }
                .mapNotNullValues { a: Annotation ->
                    a.annotationClass.findAnnotation<ConnectorHandler>()!!.connectorTypeId to
                        (
                            a.annotationClass.java.getDeclaredMethod(
                                "value",
                            ).invoke(a) as? Class<*>?
                        )?.kotlin
                }
                .toMap()
        }
    }

    private fun getConnectorIdHandlerMap(contextClass: KClass<*>): Map<String, KClass<*>> {
        return connectorIdHandlerMap.getOrPut(contextClass) {
            contextClass.findAnnotation<ConnectorIdHandlers>()?.handlers?.associate { connectorIdHandler ->
                connectorIdHandler.connectorId to connectorIdHandler.value
            } ?: mapOf()
        }
    }

    private fun getAllAnnotations(
        kClass: KClass<*>,
        alreadyFound: MutableSet<KClass<*>> = mutableSetOf(),
    ): List<Annotation> {
        return if (!alreadyFound.contains(kClass)) {
            val r = kClass.annotations.toMutableList()
            alreadyFound.add(kClass)
            kClass.superclasses.forEach {
                r.addAll(getAllAnnotations(it, alreadyFound))
            }
            r
        } else {
            emptyList()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified C : ConnectorSpecificHandling, reified T> provideConnectorStoryHandler(
        storyDef: T,
        connectorDefClass: KClass<*>?,
    ): C? {
        val p = connectorDefClass?.primaryConstructor
        return p?.callBy(
            mapOf(
                (
                    p.parameters.firstOrNull {
                        it.type.isSubtypeOf(typeOf<T>())
                    } ?: throw NoSuchElementException("Primary constructor of $connectorDefClass is missing its context parameter")
                ) to storyDef,
            ),
        ) as C?
    }

    override fun provide(
        storyDef: StoryHandlerDefinition,
        connectorType: ConnectorType,
    ): ConnectorStoryHandlerBase<*>? {
        val connectorDef = getConnectorHandlerMap(storyDef.javaClass.kotlin)[connectorType.id]
        return provideConnectorStoryHandler<ConnectorStoryHandlerBase<*>, BotBus>(storyDef, connectorDef)
    }

    override fun provide(
        storyDef: StoryHandlerDefinition,
        connectorId: String,
    ): ConnectorStoryHandlerBase<*>? {
        val connectorDef = getConnectorIdHandlerMap(storyDef.javaClass.kotlin)[connectorId]
        return provideConnectorStoryHandler<ConnectorStoryHandlerBase<*>, BotBus>(storyDef, connectorDef)
    }

    @ExperimentalTockCoroutines
    override fun provide(
        storyDef: AsyncStoryHandling,
        connectorType: ConnectorType,
    ): AsyncConnectorHandlingBase<*>? {
        val connectorDef = getConnectorHandlerMap(storyDef.javaClass.kotlin)[connectorType.id]
        return provideConnectorStoryHandler<AsyncConnectorHandlingBase<*>, AsyncStoryHandling>(storyDef, connectorDef)
    }

    @ExperimentalTockCoroutines
    override fun provide(
        storyDef: AsyncStoryHandling,
        connectorId: String,
    ): AsyncConnectorHandlingBase<*>? {
        val connectorDef = getConnectorIdHandlerMap(storyDef.javaClass.kotlin)[connectorId]
        return provideConnectorStoryHandler<AsyncConnectorHandlingBase<*>, AsyncStoryHandling>(storyDef, connectorDef)
    }
}
