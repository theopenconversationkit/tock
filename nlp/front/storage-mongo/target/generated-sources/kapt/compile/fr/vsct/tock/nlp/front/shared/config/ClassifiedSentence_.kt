package fr.vsct.tock.nlp.front.shared.config

import java.time.Instant
import java.util.Locale
import kotlin.Double
import kotlin.String
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KPropertyPath

class ClassifiedSentence_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ClassifiedSentence?>) : KPropertyPath<T, ClassifiedSentence?>(previous,property) {
    val text: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentence::text)

    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentence::language)

    val applicationId: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentence::applicationId)

    val creationDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentence::creationDate)

    val updateDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentence::updateDate)

    val status: KProperty1<T, ClassifiedSentenceStatus?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentence::status)

    val classification: Classification_<T>
        get() = Classification_(this,ClassifiedSentence::classification)

    val lastIntentProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentence::lastIntentProbability)

    val lastEntityProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentence::lastEntityProbability)
    companion object {
        val Text: KProperty1<ClassifiedSentence, String?>
            get() = ClassifiedSentence::text
        val Language: KProperty1<ClassifiedSentence, Locale?>
            get() = ClassifiedSentence::language
        val ApplicationId: KProperty1<ClassifiedSentence, Id<ApplicationDefinition>?>
            get() = ClassifiedSentence::applicationId
        val CreationDate: KProperty1<ClassifiedSentence, Instant?>
            get() = ClassifiedSentence::creationDate
        val UpdateDate: KProperty1<ClassifiedSentence, Instant?>
            get() = ClassifiedSentence::updateDate
        val Status: KProperty1<ClassifiedSentence, ClassifiedSentenceStatus?>
            get() = ClassifiedSentence::status
        val Classification: Classification_<ClassifiedSentence>
            get() = Classification_<ClassifiedSentence>(null,ClassifiedSentence::classification)
        val LastIntentProbability: KProperty1<ClassifiedSentence, Double?>
            get() = ClassifiedSentence::lastIntentProbability
        val LastEntityProbability: KProperty1<ClassifiedSentence, Double?>
            get() = ClassifiedSentence::lastEntityProbability}
}

class ClassifiedSentence_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<ClassifiedSentence>?>) : KPropertyPath<T, Collection<ClassifiedSentence>?>(previous,property) {
    val text: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentence::text)

    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentence::language)

    val applicationId: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentence::applicationId)

    val creationDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentence::creationDate)

    val updateDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentence::updateDate)

    val status: KProperty1<T, ClassifiedSentenceStatus?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentence::status)

    val classification: Classification_<T>
        get() = Classification_(this,ClassifiedSentence::classification)

    val lastIntentProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentence::lastIntentProbability)

    val lastEntityProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentence::lastEntityProbability)
}
