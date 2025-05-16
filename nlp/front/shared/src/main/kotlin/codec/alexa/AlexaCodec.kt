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

package ai.tock.nlp.front.shared.codec.alexa

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import org.litote.kmongo.Id
import java.util.Locale

/**
 * Tock model to Alexa model Codec.
 */
interface AlexaCodec {

    /**
     * Export a Tock model to an Alexa model (Skill Builder format).
     */
    fun exportIntentsSchema(
        /**
         * The invocation name.
         */
        invocationName: String,
        /**
         * The Tock application id.
         */
        applicationId: Id<ApplicationDefinition>,
        /**
         * The locale.
         */
        localeToExport: Locale,
        /**
         * A model filter if you don't want/need to export the whole model.
         */
        filter: AlexaFilter? = null,
        /**
         * A model transformer to adapt the alexa model.
         */
        transformer: AlexaModelTransformer = object : AlexaModelTransformer {
            override fun transform(schema: AlexaIntentsSchema) = schema
        }
    ): AlexaIntentsSchema
}
