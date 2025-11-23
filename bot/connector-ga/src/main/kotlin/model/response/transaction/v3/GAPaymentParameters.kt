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

import ai.tock.bot.connector.ga.model.request.transaction.v3.GAPaymentType
import ai.tock.bot.connector.ga.model.response.GAStatusCode

data class GAPaymentParameters(
    val merchantPaymentOption: GAMerchantPaymentOption?,
)

data class GAMerchantPaymentOption(
    val merchantPaymentMethod: List<GAMerchantPaymentMethod>,
    val defaultMerchantPaymentMethodId: String?,
    val managePaymentMethodUrl: String?,
)

data class GAMerchantPaymentMethod(
    val paymentMethodGroup: String?,
    val paymentMethodId: String?,
    val paymentMethodDisplayInfo: GAPaymentMethodDisplayInfo,
    val paymentMethodStatus: GAPaymentMethodStatus?,
)

data class GAPaymentMethodDisplayInfo(
    val paymentType: GAPaymentType?,
    val paymentMethodDisplayName: String?,
)

data class GAPaymentMethodStatus(
    val status: GAStatusCode?,
    val statusMessage: String?,
)

enum class GAPaymentMethodStatusValue {
    STATUS_UNSPECIFIED,
    STATUS_OK,
    STATUS_REQUIRE_FIX,
    STATUS_INAPPLICABLE,
}
