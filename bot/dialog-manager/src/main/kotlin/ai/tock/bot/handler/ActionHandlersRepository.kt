/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.handler

import ai.tock.bot.provider.SupportedNewActionHandlersProvider

/**
 * A handlers repository, it contains all shared and custom handlers
 */
object ActionHandlersRepository {

    //TODO : work in progress
    // TODO: complete the implementation

    private val handlers = mutableMapOf<String, (Map<String, String?>) -> Map<String, String?>>()

    init {
//        subscribe(Global)
//        subscribe(JoignabilitePlafondsVirement)
//        subscribe(JoignabilitePriseRDV)
//        subscribe(JoignabiliteDemandeAttestation)
//        subscribe(JoignabiliteReclamation)
//        subscribe(AuMaxActivationCarte)

        //        subscribe(AuMaxActivationCarte)

        SupportedNewActionHandlersProvider.providers().forEach {
                subscribe(it)
        }
    }

    /**
     * Subscription of handlers provider
     */
    private fun subscribe(actionHandler: NewActionHandlersProvider) {
        actionHandler.getHandlers().forEach(::add)
    }

    /**
     * Add a handler if it does not exist, or throws error exception
     */
    private fun add(handlerName: String, handlerCallback: (Map<String, String?>) -> Map<String, String?>) {
        if(handlers.contains(handlerName)) error("Handler <$handlerName> already exists")
        handlers[handlerName] = handlerCallback
    }

    /**
     * Invoke a handler if it exists, or throws error exception
     */
    fun invoke(handlerName: String, contexts: Map<String, String?>): Map<String, String?> {
        val handlerCallback = handlers[handlerName]
        handlerCallback ?: error("TickAction handler <$handlerName> not found")

        return handlerCallback.invoke(contexts)
    }

    /**
     * Checks if the map handlers contains the given handler name.
     */
    fun contains(handlerName: String) = handlers.contains(handlerName)

    /**
     * Get handlers name
     */
    fun getHandlersName(): List<String> = handlers.keys.toList()
}