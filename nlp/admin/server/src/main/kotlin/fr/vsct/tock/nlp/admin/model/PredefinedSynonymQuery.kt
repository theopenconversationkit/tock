package fr.vsct.tock.nlp.admin.model

import java.util.Locale

data class PredefinedSynonymQuery(
    val entityTypeName: String,
    val predefinedValue: String,
    val locale: Locale,
    val synonym: String
)