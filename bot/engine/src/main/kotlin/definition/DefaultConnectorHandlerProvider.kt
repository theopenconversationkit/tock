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
import ai.tock.shared.mapNotNullValues
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.superclasses

internal object DefaultConnectorHandlerProvider : ConnectorHandlerProvider {

    private val connectorHandlerMap: MutableMap<KClass<*>, Map<String, KClass<*>>> = ConcurrentHashMap()

    private val connectorIdHandlerMap: MutableMap<KClass<*>, Map<String, KClass<*>>> = ConcurrentHashMap()

    private fun getConnectorHandlerMap(storyDef: StoryHandlerDefinition): Map<String, KClass<*>> {
        val kclass = storyDef.javaClass.kotlin
        return connectorHandlerMap.getOrPut(kclass) {
            getAllAnnotations(kclass)
                .filter { it.annotationClass.findAnnotation<ConnectorHandler>() != null }
                .mapNotNullValues { a: Annotation ->
                    a.annotationClass.findAnnotation<ConnectorHandler>()!!.connectorTypeId to (
                        a.annotationClass.java.getDeclaredMethod(
                            "value"
                        ).invoke(a) as? Class<*>?
                        )?.kotlin
                }
                .toMap()
        }
    }

    private fun getConnectorIdHandlerMap(storyDef: StoryHandlerDefinition): Map<String, KClass<*>> {
        val kclass = storyDef.javaClass.kotlin
        return connectorIdHandlerMap.getOrPut(kclass) {
            kclass.findAnnotation<ConnectorIdHandlers>()?.handlers?.map { connectorIdHandler ->
                connectorIdHandler.connectorId to connectorIdHandler.value
            }?.toMap() ?: mapOf()
        }
    }

    private fun getAllAnnotations(
        kClass: KClass<*>,
        alreadyFound: MutableSet<KClass<*>> = mutableSetOf()
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
    private fun provideConnectorStoryHandler(storyDef: StoryHandlerDefinition, connectorDefClass: KClass<*>?): ConnectorStoryHandlerBase<*>? {
        val p = connectorDefClass?.primaryConstructor
        return p?.callBy(
            mapOf(
                p.parameters.first {
                    it.type.isSubtypeOf(BotBus::class.starProjectedType)
                } to storyDef
            )
        ) as ConnectorStoryHandlerBase<*>?
    }

    override fun provide(storyDef: StoryHandlerDefinition, connectorType: ConnectorType): ConnectorStoryHandlerBase<*>? {
        val connectorDef = getConnectorHandlerMap(storyDef)[connectorType.id]
        return provideConnectorStoryHandler(storyDef, connectorDef)
    }

    override fun provide(storyDef: StoryHandlerDefinition, connectorId: String): ConnectorStoryHandlerBase<*>? {
        val connectorDef = getConnectorIdHandlerMap(storyDef)[connectorId]
        return provideConnectorStoryHandler(storyDef, connectorDef)
    }
}
