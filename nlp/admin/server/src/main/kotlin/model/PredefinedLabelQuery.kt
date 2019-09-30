package ai.tock.nlp.admin.model

import java.util.Locale

data class PredefinedLabelQuery(
    val entityTypeName: String,
    val predefinedValue: String,
    val locale: Locale,
    val label: String
)