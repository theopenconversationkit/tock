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
import org.litote.kmongo.property.KMapSimplePropertyPath
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
private val __IntentErrors: KProperty1<TestBuild, Int?>
    get() = TestBuild::intentErrors
private val __EntityErrors: KProperty1<TestBuild, Int?>
    get() = TestBuild::entityErrors
private val __NbSentencesTestedByIntent: KProperty1<TestBuild, Map<String, Int>?>
    get() = TestBuild::nbSentencesTestedByIntent
private val __IntentErrorsByIntent: KProperty1<TestBuild, Map<String, Int>?>
    get() = TestBuild::intentErrorsByIntent
private val __EntityErrorsByIntent: KProperty1<TestBuild, Map<String, Int>?>
    get() = TestBuild::entityErrorsByIntent
class TestBuild_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, TestBuild?>) :
        KPropertyPath<T, TestBuild?>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val startDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__StartDate)

    val buildModelDuration: KPropertyPath<T, Duration?>
        get() = KPropertyPath(this,__BuildModelDuration)

    val testSentencesDuration: KPropertyPath<T, Duration?>
        get() = KPropertyPath(this,__TestSentencesDuration)

    val nbSentencesInModel: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__NbSentencesInModel)

    val nbSentencesTested: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__NbSentencesTested)

    val nbErrors: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__NbErrors)

    val intentErrors: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__IntentErrors)

    val entityErrors: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__EntityErrors)

    val nbSentencesTestedByIntent: KMapSimplePropertyPath<T, String?, Int?>
        get() = KMapSimplePropertyPath(this,TestBuild::nbSentencesTestedByIntent)

    val intentErrorsByIntent: KMapSimplePropertyPath<T, String?, Int?>
        get() = KMapSimplePropertyPath(this,TestBuild::intentErrorsByIntent)

    val entityErrorsByIntent: KMapSimplePropertyPath<T, String?, Int?>
        get() = KMapSimplePropertyPath(this,TestBuild::entityErrorsByIntent)

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
            get() = __NbErrors
        val IntentErrors: KProperty1<TestBuild, Int?>
            get() = __IntentErrors
        val EntityErrors: KProperty1<TestBuild, Int?>
            get() = __EntityErrors
        val NbSentencesTestedByIntent: KMapSimplePropertyPath<TestBuild, String?, Int?>
            get() = KMapSimplePropertyPath(null, __NbSentencesTestedByIntent)
        val IntentErrorsByIntent: KMapSimplePropertyPath<TestBuild, String?, Int?>
            get() = KMapSimplePropertyPath(null, __IntentErrorsByIntent)
        val EntityErrorsByIntent: KMapSimplePropertyPath<TestBuild, String?, Int?>
            get() = KMapSimplePropertyPath(null, __EntityErrorsByIntent)}
}

class TestBuild_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<TestBuild>?>) : KCollectionPropertyPath<T, TestBuild?,
        TestBuild_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val startDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__StartDate)

    val buildModelDuration: KPropertyPath<T, Duration?>
        get() = KPropertyPath(this,__BuildModelDuration)

    val testSentencesDuration: KPropertyPath<T, Duration?>
        get() = KPropertyPath(this,__TestSentencesDuration)

    val nbSentencesInModel: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__NbSentencesInModel)

    val nbSentencesTested: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__NbSentencesTested)

    val nbErrors: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__NbErrors)

    val intentErrors: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__IntentErrors)

    val entityErrors: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__EntityErrors)

    val nbSentencesTestedByIntent: KMapSimplePropertyPath<T, String?, Int?>
        get() = KMapSimplePropertyPath(this,TestBuild::nbSentencesTestedByIntent)

    val intentErrorsByIntent: KMapSimplePropertyPath<T, String?, Int?>
        get() = KMapSimplePropertyPath(this,TestBuild::intentErrorsByIntent)

    val entityErrorsByIntent: KMapSimplePropertyPath<T, String?, Int?>
        get() = KMapSimplePropertyPath(this,TestBuild::entityErrorsByIntent)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestBuild_<T> = TestBuild_(this,
            customProperty(this, additionalPath))}

class TestBuild_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        TestBuild>?>) : KMapPropertyPath<T, K, TestBuild?, TestBuild_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val startDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__StartDate)

    val buildModelDuration: KPropertyPath<T, Duration?>
        get() = KPropertyPath(this,__BuildModelDuration)

    val testSentencesDuration: KPropertyPath<T, Duration?>
        get() = KPropertyPath(this,__TestSentencesDuration)

    val nbSentencesInModel: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__NbSentencesInModel)

    val nbSentencesTested: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__NbSentencesTested)

    val nbErrors: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__NbErrors)

    val intentErrors: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__IntentErrors)

    val entityErrors: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__EntityErrors)

    val nbSentencesTestedByIntent: KMapSimplePropertyPath<T, String?, Int?>
        get() = KMapSimplePropertyPath(this,TestBuild::nbSentencesTestedByIntent)

    val intentErrorsByIntent: KMapSimplePropertyPath<T, String?, Int?>
        get() = KMapSimplePropertyPath(this,TestBuild::intentErrorsByIntent)

    val entityErrorsByIntent: KMapSimplePropertyPath<T, String?, Int?>
        get() = KMapSimplePropertyPath(this,TestBuild::entityErrorsByIntent)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestBuild_<T> = TestBuild_(this,
            customProperty(this, additionalPath))}
