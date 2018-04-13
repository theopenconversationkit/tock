package fr.vsct.tock.nlp.front.shared.test

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlin.Int
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KPropertyPath

class TestBuild_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, TestBuild?>) : KPropertyPath<T, TestBuild?>(previous,property) {
    val applicationId: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestBuild::applicationId)

    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestBuild::language)

    val startDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestBuild::startDate)

    val buildModelDuration: KProperty1<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestBuild::buildModelDuration)

    val testSentencesDuration: KProperty1<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestBuild::testSentencesDuration)

    val nbSentencesInModel: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestBuild::nbSentencesInModel)

    val nbSentencesTested: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestBuild::nbSentencesTested)

    val nbErrors: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestBuild::nbErrors)
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

class TestBuild_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<TestBuild>?>) : KPropertyPath<T, Collection<TestBuild>?>(previous,property) {
    val applicationId: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestBuild::applicationId)

    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestBuild::language)

    val startDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestBuild::startDate)

    val buildModelDuration: KProperty1<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestBuild::buildModelDuration)

    val testSentencesDuration: KProperty1<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestBuild::testSentencesDuration)

    val nbSentencesInModel: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestBuild::nbSentencesInModel)

    val nbSentencesTested: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestBuild::nbSentencesTested)

    val nbErrors: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestBuild::nbErrors)
}
