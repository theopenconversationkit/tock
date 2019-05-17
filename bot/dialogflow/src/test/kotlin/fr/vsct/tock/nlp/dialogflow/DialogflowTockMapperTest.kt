package fr.vsct.tock.nlp.dialogflow

import com.google.cloud.dialogflow.v2.QueryResult
import com.google.protobuf.ListValue
import com.google.protobuf.Struct
import com.google.protobuf.Value
import fr.vsct.tock.nlp.entity.NumberValue
import fr.vsct.tock.nlp.entity.StringValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DialogflowTockMapperTest {

    @Test
    fun `nlp result mapping`() {
        val namespace = "app"
        val intentName = "test_intent"
        val queryText = "I test my intent"
        val intentProbability = 0.8f
        val numberEntityName = "number_entity"

        val queryResultBuilder = QueryResult.newBuilder()
        queryResultBuilder.intentBuilder.setDisplayName(intentName).build()
        queryResultBuilder.queryText = queryText
        queryResultBuilder.intentDetectionConfidence = intentProbability
        queryResultBuilder.parametersBuilder.putFields(numberEntityName, Value.newBuilder().setNumberValue(666.0).build())
        queryResultBuilder.parametersBuilder.putFields("text_entity", Value.newBuilder().setStringValue("text_value").build())
        queryResultBuilder.parametersBuilder.putFields("boolean_entity", Value.newBuilder().setBoolValue(true).build())
        queryResultBuilder.parametersBuilder.putFields("struct_entity", Value.newBuilder().setStructValue(Struct.newBuilder()).build())
        queryResultBuilder.parametersBuilder.putFields("list_entity", Value.newBuilder().setListValue(ListValue.newBuilder()).build())


        val nlpResult = DialogflowTockMapper().toNlpResult(queryResultBuilder.build(), namespace)
        assertEquals(intentName, nlpResult.intent)
        assertEquals(namespace, nlpResult.intentNamespace)
        assertEquals(queryText, nlpResult.retainedQuery)
        assertEquals(intentProbability.toDouble(), nlpResult.intentProbability)
        assertEquals(nlpResult.entities.size, 3)
        assertEquals(nlpResult.entities[0].entity.entityType.name, "$namespace:$numberEntityName")
        assertEquals(nlpResult.entities[0].entity.role, numberEntityName)
        assertEquals(nlpResult.entities[0].value, NumberValue(666.0))
        assertEquals(nlpResult.entities[1].value, StringValue("text_value"))
        assertEquals(nlpResult.entities[2].value, StringValue("true"))
    }
}