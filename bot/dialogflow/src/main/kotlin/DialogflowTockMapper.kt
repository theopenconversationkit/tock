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

package ai.tock.nlp.dialogflow

import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.api.client.model.EntityType
import ai.tock.nlp.api.client.model.NlpEntityValue
import ai.tock.nlp.api.client.model.NlpResult
import ai.tock.nlp.entity.NumberValue
import ai.tock.nlp.entity.StringValue
import com.google.cloud.dialogflow.v2.QueryResult
import com.google.protobuf.Value
import java.util.Locale

internal class DialogflowTockMapper {
    /**
     * Returns a Tock entity from a Dialogflow Value.
     */
    private fun dialogflowEntityToTockEntity(
        parameterName: String,
        namespace: String,
    ): Entity? {
        return Entity(EntityType("$namespace:$parameterName"), parameterName)
    }

    /**
     * Returns a Tock [NlpEntityValue] from a Dialogflow Value.
     */
    private fun dialogflowEntityToTockEntityValue(
        parameter: Map.Entry<String, Value>,
        namespace: String,
    ): NlpEntityValue? {
        val entity = dialogflowEntityToTockEntity(parameter.key, namespace)!!
        val value: ai.tock.nlp.entity.Value? =
            when (parameter.value.kindCase) {
                Value.KindCase.NUMBER_VALUE -> NumberValue(parameter.value.numberValue)
                Value.KindCase.STRING_VALUE ->
                    if (parameter.value.stringValue.trim().isEmpty()) {
                        null
                    } else {
                        StringValue(
                            parameter.value.stringValue,
                        )
                    }
                Value.KindCase.BOOL_VALUE -> StringValue(parameter.value.boolValue.toString())
                else -> null
            }

        if (value.toString().trim().isEmpty() || value == null) {
            return null
        }

        return NlpEntityValue(
            0,
            0,
            entity,
            value,
        )
    }

    fun toNlpResult(
        queryResult: QueryResult,
        namespace: String,
    ): NlpResult {
        val intent = queryResult.intent.displayName

        val parameters =
            queryResult.parameters?.fieldsMap?.filter {
                !it.value.hasStructValue() && !it.value.hasListValue() && dialogflowEntityToTockEntity(
                    it.key,
                    namespace,
                ) != null
            } ?: emptyMap()
        val entityValues =
            parameters.map {
                dialogflowEntityToTockEntityValue(it, namespace)
            }

        return NlpResult(
            intent,
            namespace,
            Locale(queryResult.languageCode),
            entityValues.filterNotNull(),
            emptyList(),
            queryResult.intentDetectionConfidence.toDouble(),
            1.0,
            queryResult.queryText,
            staticResponse = queryResult.fulfillmentText.takeIf { it.trim().isNotEmpty() },
        )
    }
}
