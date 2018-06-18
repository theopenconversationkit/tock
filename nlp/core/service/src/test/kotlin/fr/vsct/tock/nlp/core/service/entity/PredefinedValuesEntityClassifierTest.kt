package fr.vsct.tock.nlp.core.service.entity

import fr.vsct.tock.nlp.core.Entity
import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.core.PredefinedValue
import fr.vsct.tock.nlp.model.EntityCallContextForIntent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.Locale

class PredefinedValuesEntityClassifierTest {

    @Test
    fun qualify_predefined_values() {

        val text = "Je voudrais manger une napolitaine"

        val entityType = EntityType(
            "pizza", predefinedValues = listOf(
                PredefinedValue(
                    "pizza", mapOf(
                        Pair(Locale.FRENCH, listOf("4 fromages", "napolitaine", "calzone")),
                        Pair(Locale.ITALIAN, listOf("4 formaggi", "napoletana", "calzone"))
                    )
                )
            )
        )

        val context = EntityCallContextForIntent(
            "pizzayolo",
            Intent("eat", listOf(Entity(entityType, "pizza"))),
            Locale.FRENCH,
            NlpEngineType.stanford,
            ZonedDateTime.now())

        val entityTypeRecognitions = PredefinedValuesEntityClassifier.classifyEntities(context, text, arrayOf())

        Assertions.assertEquals(listOf(
            EntityTypeRecognition(
                EntityTypeValue(23, 34, entityType, "pizza", true), 1.0)),
            entityTypeRecognitions)

    }

}