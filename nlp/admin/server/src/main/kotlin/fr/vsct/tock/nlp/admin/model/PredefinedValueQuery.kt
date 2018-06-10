package fr.vsct.tock.nlp.admin.model

data class PredefinedValueQuery(
    val entityTypeName: String,
    val predefinedValue: String,
    val oldPredefinedValue: String?
)