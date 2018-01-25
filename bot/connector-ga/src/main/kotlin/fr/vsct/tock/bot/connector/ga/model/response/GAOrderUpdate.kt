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

package fr.vsct.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#orderupdate
 */
data class GAOrderUpdate(
        val googleOrderId: String,
        val actionOrderId: String,
        val orderState: GAOrderState,
        val orderManagementActions: List<GAAction>,
        val receipt: GAReceipt,
        val updateTime: String,
        val totalPrice: GAPrice,
        //TODO
        val lineItemUpdates: GALineItemUpdate,
        //TODO
        val infoExtension: String,
        val userNotification: GAUserNotification,
        val rejectionInfo: GARejectionInfo,
        val cancellationInfo: GACancellationInfo,
        val inTransitInfo: GAInTransitInfo,
        val fulfillmentInfo: GAFulfillmentInfo,
        val returnInfo: GAReturnInfo
)

