package fr.vsct.tock.nlp.core.service.entity

import fr.vsct.tock.nlp.core.DictionaryData
import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.core.PredefinedValue
import fr.vsct.tock.nlp.entity.StringValue
import fr.vsct.tock.nlp.model.EntityCallContextForEntity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PredefinedValuesEntityEvaluatorTest {

    private val context = EntityCallContextForEntity(
        EntityType("namespace:frequency", dictionary = true),
        Locale.FRENCH,
        NlpEngineType.stanford,
        "test",
        ZonedDateTime.now())

    @BeforeEach
    fun fillDictionary() {
        DictionaryRepositoryService.updateData(
            listOf(
                DictionaryData(
                    "namespace",
                    "frequency",
                    listOf(
                        PredefinedValue("Annuel", mapOf(
                            Pair(Locale.FRENCH, listOf("Année", "annee")))),
                        PredefinedValue("Mensuel", mapOf(
                            Pair(Locale.FRENCH, listOf("Mois")))),
                        PredefinedValue("Semaine", mapOf(
                            Pair(Locale.FRENCH, listOf("Hebdomadaire")),
                            Pair(Locale.ENGLISH, listOf("Week")))),
                        PredefinedValue("Jour", mapOf(
                            Pair(Locale.FRENCH, listOf("Journalier", "Quotidien"))))
                    ))))
    }

    @AfterEach
    fun cleanupDictionary() {
        DictionaryRepositoryService.updateData(emptyList())
    }

    @Test
    fun should_evaluate_frequency_day_with_synonym_value_found() {

        val evaluationResult = DictionaryEntityTypeEvaluator.evaluate(context, "Quotidien")

        assertTrue(evaluationResult.evaluated)
        assertEquals(1.0, evaluationResult.probability)
        assertEquals("Jour", (evaluationResult.value as StringValue).value)

    }

    @Test
    fun should_evaluate_frequency_week_with_synonym_value_not_found() {

        val evaluationResult = DictionaryEntityTypeEvaluator.evaluate(context, "Week")

        assertTrue(evaluationResult.evaluated)
        assertEquals(1.0, evaluationResult.probability)
        assertNull(evaluationResult.value)
    }

    @Test
    fun should_evaluate_frequency_day_with_synonym_value_not_found_but_near() {

        val evaluationResult = DictionaryEntityTypeEvaluator.evaluate(context, "Quotidienne")

        assertTrue(evaluationResult.evaluated)
        assertEquals(0.8181818127632141, evaluationResult.probability)
        assertEquals("Jour", (evaluationResult.value as StringValue).value)

    }

}