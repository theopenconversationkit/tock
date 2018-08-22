package fr.vsct.tock.nlp.front.shared.test

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

class TestBuild_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, TestBuild?>) : KPropertyPath<T, TestBuild?>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,TestBuild::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,TestBuild::language)

    val startDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,TestBuild::startDate)

    val buildModelDuration: KPropertyPath<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Duration?>(this,TestBuild::buildModelDuration)

    val testSentencesDuration: KPropertyPath<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Duration?>(this,TestBuild::testSentencesDuration)

    val nbSentencesInModel: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,TestBuild::nbSentencesInModel)

    val nbSentencesTested: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,TestBuild::nbSentencesTested)

    val nbErrors: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,TestBuild::nbErrors)
    companion object {
        val ApplicationId: KProperty1<TestBuild, Id<ApplicationDefinition>?>
            get() = TestBuild::applicationId
        val Language: KProperty1<TestBuild, Locale?>
            get() = TestBuild::language
        val StartDate: KProperty1<TestBuild, Instant?>
            get() = TestBuild::startDate
        val BuildModelDuration: KProperty1<TestBuild, Duration?>
            get() = TestBuild::buildModelDuration
        val TestSentencesDuration: KProperty1<TestBuild, Duration?>
            get() = TestBuild::testSentencesDuration
        val NbSentencesInModel: KProperty1<TestBuild, Int?>
            get() = TestBuild::nbSentencesInModel
        val NbSentencesTested: KProperty1<TestBuild, Int?>
            get() = TestBuild::nbSentencesTested
        val NbErrors: KProperty1<TestBuild, Int?>
            get() = TestBuild::nbErrors}
}

class TestBuild_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<TestBuild>?>) : KCollectionPropertyPath<T, TestBuild?, TestBuild_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,TestBuild::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,TestBuild::language)

    val startDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,TestBuild::startDate)

    val buildModelDuration: KPropertyPath<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Duration?>(this,TestBuild::buildModelDuration)

    val testSentencesDuration: KPropertyPath<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Duration?>(this,TestBuild::testSentencesDuration)

    val nbSentencesInModel: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,TestBuild::nbSentencesInModel)

    val nbSentencesTested: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,TestBuild::nbSentencesTested)

    val nbErrors: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,TestBuild::nbErrors)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestBuild_<T> = TestBuild_(this, customProperty(this, additionalPath))}

class TestBuild_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, TestBuild>?>) : KMapPropertyPath<T, K, TestBuild?, TestBuild_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,TestBuild::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,TestBuild::language)

    val startDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,TestBuild::startDate)

    val buildModelDuration: KPropertyPath<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Duration?>(this,TestBuild::buildModelDuration)

    val testSentencesDuration: KPropertyPath<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Duration?>(this,TestBuild::testSentencesDuration)

    val nbSentencesInModel: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,TestBuild::nbSentencesInModel)

    val nbSentencesTested: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,TestBuild::nbSentencesTested)

    val nbErrors: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,TestBuild::nbErrors)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestBuild_<T> = TestBuild_(this, customProperty(this, additionalPath))}
