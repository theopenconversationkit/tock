package fr.vsct.tock.nlp.core.service.entity

import fr.vsct.tock.nlp.core.PredefinedValue
import fr.vsct.tock.nlp.model.EntityCallContextForEntity
import mu.KotlinLogging
import java.util.Locale

object PredefinedValuesEntityEvaluator : EntityEvaluator/*, EntityTypeClassifier*/ {

    private val logger = KotlinLogging.logger {}

    override fun evaluate(context: EntityCallContextForEntity, text: String): EvaluationResult {

        val predefinedValues = localizedPredefinedValues(context)

        val predefinedValue = predefinedValueOfSynonym(context.language, predefinedValues, text)

        return EvaluationResult(true, predefinedValue?.value, 1.0)
    }

    private fun localizedPredefinedValues(context: EntityCallContextForEntity): Map<PredefinedValue, List<String>?> {
        return context
            .entityType
            .predefinedValues
            .associate { predefinedValue -> predefinedValue to predefinedValue.synonyms[context.language] }
    }

    private fun predefinedValueOfSynonym(
        locale: Locale,
        predefinedValues: Map<PredefinedValue, List<String>?>,
        text: String
    ): PredefinedValue? {
        for (predefinedValue in predefinedValues.keys) {
            val synonyms = predefinedValues[predefinedValue]
            if (synonyms != null && synonyms.find { s -> s.toLowerCase(locale) == text.toLowerCase(locale) } != null) {
                return predefinedValue
            }
        }
        return null
    }

}