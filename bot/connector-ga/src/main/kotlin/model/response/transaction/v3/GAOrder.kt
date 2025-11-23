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

package ai.tock.bot.connector.ga.model.response.transaction.v3

import ai.tock.bot.connector.ga.model.response.GAImage
import ai.tock.bot.connector.ga.model.response.GAMerchant

/**
 * @see https://developers.google.com/actions/transactions/reference/physical/rest/v3/Order
 */
data class GAOrder(
    val googleOrderId: String? = null,
    val merchantOrderId: String,
    val buyerInfo: GAUserInfo? = null,
    val image: GAImage? = null,
    val createTime: String,
    val lastUpdateTime: String? = null,
    val transactionMerchant: GAMerchant? = null,
    val contents: GAContents,
    val priceAttributes: List<GAPriceAttribute>? = emptyList(),
    val followUpActions: List<GAActionV3>? = emptyList(),
    val termsOfServiceUrl: String? = null,
    val note: String? = null,
    val purchase: GAPurchaseOrderExtension? = null,
    val ticket: GATicketOrderExtension? = null,
)
