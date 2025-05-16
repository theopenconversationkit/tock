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

package ai.tock.bot.connector.ga.model.request

/**
 *
 */
enum class GAArgumentBuiltInName {

    /** Permission granted argument. */
    PERMISSION,
    /** Option selected argument. */
    OPTION,
    /** Transaction requirements check result argument. */
    TRANSACTION_REQUIREMENTS_CHECK_RESULT,
    /** Delivery address value argument. */
    DELIVERY_ADDRESS_VALUE,
    /** Transactions decision argument. */
    TRANSACTION_DECISION_VALUE,
    /** Confirmation argument. */
    CONFIRMATION,
    /** DateTime argument. */
    DATETIME,
    /** Sign in status argument. */
    SIGN_IN,
    /** New Surface status argument. */
    NEW_SURFACE,
    /** Media status argument. */
    MEDIA_STATUS
}
