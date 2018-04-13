package fr.vsct.tock.nlp.front.shared.test

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedEntity_Col
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import java.time.Instant
import java.util.Locale
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KPropertyPath

class EntityTestError_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, EntityTestError?>) : KPropertyPath<T, EntityTestError?>(previous,property) {
    val applicationId: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTestError::applicationId)

    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTestError::language)

    val text: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTestError::text)

    val intentId: KProperty1<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTestError::intentId)

    val lastAnalyse: ClassifiedEntity_Col<T>
        get() = ClassifiedEntity_Col(this,EntityTestError::lastAnalyse)

    val averageErrorProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTestError::averageErrorProbability)

    val count: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTestError::count)

    val total: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTestError::total)

    val firstDetectionDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTestError::firstDetectionDate)
    companion object {
        val ApplicationId: KProperty1<EntityTestError, Id<ApplicationDefinition>?>
            get() = EntityTestError::applicationId
        val Language: KProperty1<EntityTestError, Locale?>
            get() = EntityTestError::language
        val Text: KProperty1<EntityTestError, String?>
            get() = EntityTestError::text
        val IntentId: KProperty1<EntityTestError, Id<IntentDefinition>?>
            get() = EntityTestError::intentId
        val LastAnalyse: ClassifiedEntity_Col<EntityTestError>
            get() = ClassifiedEntity_Col<EntityTestError>(null,EntityTestError::lastAnalyse)
        val AverageErrorProbability: KProperty1<EntityTestError, Double?>
            get() = EntityTestError::averageErrorProbability
        val Count: KProperty1<EntityTestError, Int?>
            get() = EntityTestError::count
        val Total: KProperty1<EntityTestError, Int?>
            get() = EntityTestError::total
        val FirstDetectionDate: KProperty1<EntityTestError, Instant?>
            get() = EntityTestError::firstDetectionDate}
}

class EntityTestError_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<EntityTestError>?>) : KPropertyPath<T, Collection<EntityTestError>?>(previous,property) {
    val applicationId: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTestError::applicationId)

    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTestError::language)

    val text: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTestError::text)

    val intentId: KProperty1<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTestError::intentId)

    val lastAnalyse: ClassifiedEntity_Col<T>
        get() = ClassifiedEntity_Col(this,EntityTestError::lastAnalyse)

    val averageErrorProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTestError::averageErrorProbability)

    val count: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTestError::count)

    val total: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTestError::total)

    val firstDetectionDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTestError::firstDetectionDate)
}
