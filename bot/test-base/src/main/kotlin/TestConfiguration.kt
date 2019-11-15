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

package ai.tock.bot.test

import ai.tock.bot.connector.ConnectorProvider
import ai.tock.bot.connector.ConnectorType
import java.util.ServiceLoader

/**
 * Default connector used for tests.
 */
@Volatile
var defaultTestConnectorType: ConnectorType = ServiceLoader.load(ConnectorProvider::class.java).toList().run {
    //legacy
    firstOrNull { it.connectorType.id == "messenger" }?.connectorType
    //takes the first
            ?: filter { it.connectorType != ConnectorType.rest }.sortedBy { it.connectorType.id }.firstOrNull()?.connectorType
            //default to rest
            ?: ConnectorType.rest
}