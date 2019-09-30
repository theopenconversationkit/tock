package ai.tock.nlp.admin.model

import java.util.Locale

data class PredefinedValueQuery(
    val entityTypeName: String,
    val predefinedValue: String,
    val locale: Locale,
    val oldPredefinedValue: String?
)