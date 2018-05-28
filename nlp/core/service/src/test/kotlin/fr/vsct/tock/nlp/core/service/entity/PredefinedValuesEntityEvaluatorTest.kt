package fr.vsct.tock.nlp.core.service.entity

import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.core.PredefinedValue
import fr.vsct.tock.nlp.model.EntityCallContextForEntity
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.test.assertTrue

class PredefinedValuesEntityEvaluatorTest {

    private val context = EntityCallContextForEntity(
        EntityType(
            "frequency:tock",
            predefinedValues = listOf(
                PredefinedValue("Annuel", mapOf(
                    Pair(Locale.FRENCH, listOf("Année", "annee")))),
                PredefinedValue("Mensuel", mapOf(
                    Pair(Locale.FRENCH, listOf("Mois")))),
                PredefinedValue("Semaine", mapOf(
                    Pair(Locale.FRENCH, listOf("Hebdomadaire")),
                    Pair(Locale.ENGLISH, listOf("Week")))),
                PredefinedValue("Jour", mapOf(
                    Pair(Locale.FRENCH, listOf("Journalier", "Quotidien"))))
            )),
        Locale.FRENCH,
        NlpEngineType.stanford,
        ZonedDateTime.now())

    @Test
    fun should_evaluate_frequency_day_with_synonym_value_found() {

        val evaluationResult = PredefinedValuesEntityEvaluator.evaluate(context, "Quotidien")

        assertTrue { evaluationResult.evaluated }
        assertTrue { evaluationResult.probability == 1.0 }
        assertTrue { evaluationResult.value == "Jour" }

    }

    @Test
    fun should_evaluate_frequency_week_with_synonym_value_not_found() {

        val evaluationResult = PredefinedValuesEntityEvaluator.evaluate(context, "Week")

        assertTrue { evaluationResult.evaluated }
        assertTrue { evaluationResult.probability == 1.0 }
        assertTrue { evaluationResult.value == null }

    }

    @Test
    fun should_evaluate_frequency_day_with_synonym_value_not_found() {

        val evaluationResult = PredefinedValuesEntityEvaluator.evaluate(context, "Quotidienne")

        assertTrue { evaluationResult.evaluated }
        assertTrue { evaluationResult.probability == 1.0 }
        assertTrue { evaluationResult.value == null }

    }

}