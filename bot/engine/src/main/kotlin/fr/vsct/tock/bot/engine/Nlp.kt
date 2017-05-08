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

package fr.vsct.tock.bot.engine

import ft.vsct.tock.nlp.api.client.NlpClient
import ft.vsct.tock.nlp.api.client.model.NlpQuery
import ft.vsct.tock.nlp.api.client.model.NlpResult
import mu.KotlinLogging
import java.io.InputStream

/**
 * Send NLP requests.
 */
object Nlp {

    private val logger = KotlinLogging.logger {}
    private val nlpClient = NlpClient()

    fun parse(request: NlpQuery): NlpResult? {
        val response = nlpClient.parse(request)
        val result = response.body()
        if (result == null) {
            logger.error { "nlp error : ${response.errorBody().string()}" }
        }
        return result
    }

    fun importNlpDump(stream: InputStream): Boolean = nlpClient.importNlpDump(stream).body()
}