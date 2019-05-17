package fr.vsct.tock.nlp.dialogflow

import com.google.cloud.dialogflow.v2.QueryResult
import com.google.protobuf.Value
import fr.vsct.tock.nlp.api.client.model.Entity
import fr.vsct.tock.nlp.api.client.model.EntityType
import fr.vsct.tock.nlp.api.client.model.NlpEntityValue
import fr.vsct.tock.nlp.api.client.model.NlpResult
import fr.vsct.tock.nlp.entity.NumberValue
import fr.vsct.tock.nlp.entity.StringValue
import java.util.Locale

internal class DialogflowTockMapper {

    /**
     * Returns a Tock entity from a Dialogflow Value.
     */
    private fun dialogflowEntityToTockEntity(
        parameterName: String,
        namespace: String
    ): Entity? {
        return Entity(EntityType("$namespace:$parameterName"), parameterName)
    }

    /**
     * Returns a Tock [NlpEntityValue] from a Dialogflow Value.
     */
    private fun dialogflowEntityToTockEntityValue(
        parameter: Map.Entry<String, Value>,
        namespace: String
    ): NlpEntityValue? {
        val entity = dialogflowEntityToTockEntity(parameter.key, namespace)!!
        val value: fr.vsct.tock.nlp.entity.Value? = when (parameter.value.kindCase) {
            Value.KindCase.NUMBER_VALUE -> NumberValue(parameter.value.numberValue)
            Value.KindCase.STRING_VALUE -> if (parameter.value.stringValue.trim().isEmpty()) null else StringValue(
                parameter.value.stringValue
            )
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
            value
        )
    }

    fun toNlpResult(queryResult: QueryResult, namespace: String): NlpResult {
        val intent = queryResult.intent.displayName

        val parameters = queryResult.parameters?.fieldsMap?.filter {
            !it.value.hasStructValue() && !it.value.hasListValue() && dialogflowEntityToTockEntity(
                it.key,
                namespace
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
            staticResponse = queryResult.fulfillmentText.takeIf { it.trim().isNotEmpty() }
        )
    }

}