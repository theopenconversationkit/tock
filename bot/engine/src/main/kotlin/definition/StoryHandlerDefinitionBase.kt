/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.BotBus
import ai.tock.shared.mapNotNullValues
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.superclasses

/**
 * Base implementation of [StoryHandlerDefinition].
 */
abstract class StoryHandlerDefinitionBase<T : ConnectorStoryHandlerBase<*>>(val bus: BotBus) : BotBus by bus,
    StoryHandlerDefinition {

    companion object {

        private val logger = KotlinLogging.logger {}

        private val connectorHandlerMap: MutableMap<KClass<*>, Map<String, KClass<*>>> = ConcurrentHashMap()

        private fun getHandlerMap(kclass: KClass<*>): Map<String, KClass<*>> {
            return connectorHandlerMap.getOrPut(kclass) {
                getAllAnnotations(kclass)
                    .filter { it.annotationClass.findAnnotation<ConnectorHandler>() != null }
                    .mapNotNullValues { a: Annotation ->
                        a.annotationClass.findAnnotation<ConnectorHandler>()!!.connectorTypeId to (a.annotationClass.java.getDeclaredMethod(
                            "value"
                        ).invoke(a) as? Class<*>?)?.kotlin
                    }
                    .toMap()
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
    }

    /**
     * The method to implement if there is no [StoryStep] in the [StoryDefinition]
     * or when current [StoryStep] is null
     */
    open fun answer() {}

    /**
     * Default implementation redirect to answer.
     */
    override fun handle() {
        answer()
    }

    /**
     * Shortcut for [BotBus.targetConnectorType].
     */
    val connectorType: ConnectorType = bus.targetConnectorType

    /**
     * Method to override in order to provide [ConnectorStoryHandler].
     * Default implementation use annotations annotated with @[ConnectorHandler].
     */
    @Suppress("UNCHECKED_CAST")
    open fun findConnector(connectorType: ConnectorType): T? {
        val connectorDef = getHandlerMap(this::class)[connectorType.id]
        return connectorDef?.primaryConstructor?.callBy(
            mapOf(
                connectorDef.primaryConstructor!!.parameters.first {
                    it.type.isSubtypeOf(BotBus::class.starProjectedType)
                } to this
            )
        ) as T?
    }

    private val cachedConnector: T? by lazy {
        findConnector(connectorType)
            .also { if (it == null) logger.warn { "unsupported connector type $connectorType for ${this::class}" } }
    }

    /**
     * Provides the current [ConnectorStoryHandler] using [findConnector].
     */
    override val connector: T? get() = cachedConnector


}