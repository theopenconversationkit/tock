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

private val __ApplicationId: KProperty1<TestBuild, Id<ApplicationDefinition>?>
    get() = TestBuild::applicationId
private val __Language: KProperty1<TestBuild, Locale?>
    get() = TestBuild::language
private val __StartDate: KProperty1<TestBuild, Instant?>
    get() = TestBuild::startDate
private val __BuildModelDuration: KProperty1<TestBuild, Duration?>
    get() = TestBuild::buildModelDuration
private val __TestSentencesDuration: KProperty1<TestBuild, Duration?>
    get() = TestBuild::testSentencesDuration
private val __NbSentencesInModel: KProperty1<TestBuild, Int?>
    get() = TestBuild::nbSentencesInModel
private val __NbSentencesTested: KProperty1<TestBuild, Int?>
    get() = TestBuild::nbSentencesTested
private val __NbErrors: KProperty1<TestBuild, Int?>
    get() = TestBuild::nbErrors
class TestBuild_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, TestBuild?>) :
        KPropertyPath<T, TestBuild?>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath<T, Id<ApplicationDefinition>?>(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath<T, Locale?>(this,__Language)

    val startDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath<T, Instant?>(this,__StartDate)

    val buildModelDuration: KPropertyPath<T, Duration?>
        get() = KPropertyPath<T, Duration?>(this,__BuildModelDuration)

    val testSentencesDuration: KPropertyPath<T, Duration?>
        get() = KPropertyPath<T, Duration?>(this,__TestSentencesDuration)

    val nbSentencesInModel: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__NbSentencesInModel)

    val nbSentencesTested: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__NbSentencesTested)

    val nbErrors: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__NbErrors)

    companion object {
        val ApplicationId: KProperty1<TestBuild, Id<ApplicationDefinition>?>
            get() = __ApplicationId
        val Language: KProperty1<TestBuild, Locale?>
            get() = __Language
        val StartDate: KProperty1<TestBuild, Instant?>
            get() = __StartDate
        val BuildModelDuration: KProperty1<TestBuild, Duration?>
            get() = __BuildModelDuration
        val TestSentencesDuration: KProperty1<TestBuild, Duration?>
            get() = __TestSentencesDuration
        val NbSentencesInModel: KProperty1<TestBuild, Int?>
            get() = __NbSentencesInModel
        val NbSentencesTested: KProperty1<TestBuild, Int?>
            get() = __NbSentencesTested
        val NbErrors: KProperty1<TestBuild, Int?>
            get() = __NbErrors}
}

class TestBuild_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<TestBuild>?>) : KCollectionPropertyPath<T, TestBuild?,
        TestBuild_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath<T, Id<ApplicationDefinition>?>(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath<T, Locale?>(this,__Language)

    val startDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath<T, Instant?>(this,__StartDate)

    val buildModelDuration: KPropertyPath<T, Duration?>
        get() = KPropertyPath<T, Duration?>(this,__BuildModelDuration)

    val testSentencesDuration: KPropertyPath<T, Duration?>
        get() = KPropertyPath<T, Duration?>(this,__TestSentencesDuration)

    val nbSentencesInModel: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__NbSentencesInModel)

    val nbSentencesTested: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__NbSentencesTested)

    val nbErrors: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__NbErrors)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestBuild_<T> = TestBuild_(this,
            customProperty(this, additionalPath))}

class TestBuild_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        TestBuild>?>) : KMapPropertyPath<T, K, TestBuild?, TestBuild_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath<T, Id<ApplicationDefinition>?>(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath<T, Locale?>(this,__Language)

    val startDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath<T, Instant?>(this,__StartDate)

    val buildModelDuration: KPropertyPath<T, Duration?>
        get() = KPropertyPath<T, Duration?>(this,__BuildModelDuration)

    val testSentencesDuration: KPropertyPath<T, Duration?>
        get() = KPropertyPath<T, Duration?>(this,__TestSentencesDuration)

    val nbSentencesInModel: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__NbSentencesInModel)

    val nbSentencesTested: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__NbSentencesTested)

    val nbErrors: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__NbErrors)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestBuild_<T> = TestBuild_(this,
            customProperty(this, additionalPath))}
