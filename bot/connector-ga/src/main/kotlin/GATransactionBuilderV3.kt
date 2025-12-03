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

package ai.tock.bot.connector.ga

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ga.model.GAIntent
import ai.tock.bot.connector.ga.model.request.transaction.v3.GATransactionDecisionValueV3
import ai.tock.bot.connector.ga.model.request.transaction.v3.GATransactionRequirementsCheckResultV3
import ai.tock.bot.connector.ga.model.response.GAExpectedIntent
import ai.tock.bot.connector.ga.model.response.GAStructuredResponse
import ai.tock.bot.connector.ga.model.response.transaction.v3.GAOrder
import ai.tock.bot.connector.ga.model.response.transaction.v3.GAOrderOptionsV3
import ai.tock.bot.connector.ga.model.response.transaction.v3.GAOrderUpdateV3
import ai.tock.bot.connector.ga.model.response.transaction.v3.GAPaymentParameters
import ai.tock.bot.connector.ga.model.response.transaction.v3.GAPresentationOptions
import ai.tock.bot.connector.ga.model.response.transaction.v3.GATransactionDecisionValueSpecV3
import ai.tock.bot.connector.ga.model.response.transaction.v3.GATransactionRequirementsCheckSpecV3
import ai.tock.bot.engine.I18nTranslator

/**
 * Build a [GATransactionRequirementsCheckSpecV3] response.
 */
fun I18nTranslator.gaTransactionRequirementsCheckV3(): GAResponseConnectorMessage =
    gaMessage(
        GAExpectedIntent(
            GAIntent.transactionRequirementsCheckV3,
            GATransactionRequirementsCheckSpecV3(),
        ),
    )

/**
 * Return a [GATransactionRequirementsCheckResultV3] if available.
 */
fun ConnectorMessage.findTransactionRequirementsCheckResultV3(): GATransactionRequirementsCheckResultV3? = findTransactionObject(GAIntent.transactionRequirementsCheckV3)

private inline fun <reified T> ConnectorMessage.findTransactionObject(intent: GAIntent): T? =
    (this as? GARequestConnectorMessage)?.run {
        request.inputs
            .find {
                it.intent == intent.value
            }?.run {
                arguments
                    ?.map { it.extension }
                    ?.filterIsInstance<T>()
                    ?.firstOrNull()
            }
    }

/**
 * Build an [GATransactionDecisionValueSpecV3] message.
 */
fun I18nTranslator.gaTransactionOrderDecision(
    order: GAOrder,
    orderOptions: GAOrderOptionsV3? = null,
    paymentParameters: GAPaymentParameters? = null,
    presentationOptions: GAPresentationOptions? = null,
): GAResponseConnectorMessage =
    gaMessage(
        GAExpectedIntent(
            GAIntent.transactionDecisionV3,
            GATransactionDecisionValueSpecV3(
                order,
                orderOptions,
                paymentParameters,
                presentationOptions,
            ),
        ),
    )

/**
 * Return a [GATransactionDecisionValueV3] if available.
 */
fun ConnectorMessage.findTransactionDecisionValueV3(): GATransactionDecisionValueV3? = findTransactionObject(GAIntent.transactionDecisionV3)

/**
 * Build an [GAStructuredResponse] from an [GAOrderUpdateV3].
 */
fun I18nTranslator.gaTransactionOrderUpdate(orderUpdate: GAOrderUpdateV3): GAResponseConnectorMessage =
    gaMessage(
        richResponse(
            item(
                GAStructuredResponse(
                    orderUpdateV3 = orderUpdate,
                ),
            ),
        ),
    )
