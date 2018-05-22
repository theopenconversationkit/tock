package fr.vsct.tock.nlp.core.service.entity

import fr.vsct.tock.nlp.model.EntityCallContextForEntity
import mu.KotlinLogging

object PredefinedValuesEntityEvaluator : EntityEvaluator/*, EntityTypeClassifier*/ {

    private val logger = KotlinLogging.logger {}

    override fun evaluate(context: EntityCallContextForEntity, text: String): EvaluationResult {
        return EvaluationResult(
            true,
            context.entityType.predefinedValues.find {
                it.synonyms[context.language]?.contains(text.toLowerCase()) ?: false
            }?.value
        )
    }

}