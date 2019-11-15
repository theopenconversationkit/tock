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

package ai.tock.bot.connector.rest.client.model

/**
 *
 */
data class ClientGenericMessage(
        val connectorType: ClientConnectorType,
        val attachments: List<ClientAttachment> = emptyList(),
        val choices: List<ClientChoice> = emptyList(),
        //a qualified text map (ie "title" to "Ok computer", "subtitle" to "please listen")
        val texts: Map<String, String> = emptyMap(),
        val locations: List<ClientLocation> = emptyList(),
        val metadata: Map<String, String> = emptyMap(),
        val subElements: List<ClientGenericElement> = emptyList()) {
}