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

package fr.vsct.tock.nlp.front.service

import fr.vsct.tock.nlp.core.CallContext
import fr.vsct.tock.nlp.front.service.ApplicationConfigurationService.getIntentIdForIntentName
import fr.vsct.tock.nlp.front.service.FrontRepository.config
import fr.vsct.tock.nlp.front.service.FrontRepository.core
import fr.vsct.tock.nlp.front.service.FrontRepository.toApplication
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
import fr.vsct.tock.nlp.front.shared.Parser
import fr.vsct.tock.nlp.front.shared.parser.QueryDescription
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence

/**
 *
 */
object ParserService : Parser {

    override fun parse(query: QueryDescription): ParseResult {
        //TODO validate text ("no \n\r\t")
        with(query) {
            val application = config.getApplicationByNamespaceAndName(namespace, applicationName) ?: error("unknown application $namespace:$applicationName")
            val callContext = CallContext(toApplication(application), context.language, context.engineType)
            //TODO multi query handling
            //TODO state handling
            val parseResult = core.parse(callContext, query.queries.first())

            val result = ParseResult(
                    parseResult.intent,
                    parseResult.entities,
                    parseResult.intentProbability,
                    parseResult.entitiesProbability,
                    query.queries.first())

            if (context.registerQuery) {
                val intentId = getIntentIdForIntentName(result.intent)
                val sentence = ClassifiedSentence(result, context.language, application._id!!, intentId)
                config.save(sentence)
            }

            return result
        }
    }

}