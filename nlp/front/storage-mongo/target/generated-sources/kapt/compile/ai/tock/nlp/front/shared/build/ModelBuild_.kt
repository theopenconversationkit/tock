package ai.tock.nlp.front.shared.build

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlin.Boolean
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

private val __ApplicationId: KProperty1<ModelBuild, Id<ApplicationDefinition>?>
    get() = ModelBuild::applicationId
private val __Language: KProperty1<ModelBuild, Locale?>
    get() = ModelBuild::language
private val __Type: KProperty1<ModelBuild, ModelBuildType?>
    get() = ModelBuild::type
private val __IntentId: KProperty1<ModelBuild, Id<IntentDefinition>?>
    get() = ModelBuild::intentId
private val __EntityTypeName: KProperty1<ModelBuild, String?>
    get() = ModelBuild::entityTypeName
private val __NbSentences: KProperty1<ModelBuild, Int?>
    get() = ModelBuild::nbSentences
private val __Duration: KProperty1<ModelBuild, Duration?>
    get() = ModelBuild::duration
private val __Error: KProperty1<ModelBuild, Boolean?>
    get() = ModelBuild::error
private val __ErrorMessage: KProperty1<ModelBuild, String?>
    get() = ModelBuild::errorMessage
private val __Date: KProperty1<ModelBuild, Instant?>
    get() = ModelBuild::date
class ModelBuild_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ModelBuild?>) :
        KPropertyPath<T, ModelBuild?>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val type: KPropertyPath<T, ModelBuildType?>
        get() = KPropertyPath(this,__Type)

    val intentId: KPropertyPath<T, Id<IntentDefinition>?>
        get() = KPropertyPath(this,__IntentId)

    val entityTypeName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__EntityTypeName)

    val nbSentences: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__NbSentences)

    val duration: KPropertyPath<T, Duration?>
        get() = KPropertyPath(this,__Duration)

    val error: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Error)

    val errorMessage: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ErrorMessage)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    companion object {
        val ApplicationId: KProperty1<ModelBuild, Id<ApplicationDefinition>?>
            get() = __ApplicationId
        val Language: KProperty1<ModelBuild, Locale?>
            get() = __Language
        val Type: KProperty1<ModelBuild, ModelBuildType?>
            get() = __Type
        val IntentId: KProperty1<ModelBuild, Id<IntentDefinition>?>
            get() = __IntentId
        val EntityTypeName: KProperty1<ModelBuild, String?>
            get() = __EntityTypeName
        val NbSentences: KProperty1<ModelBuild, Int?>
            get() = __NbSentences
        val Duration: KProperty1<ModelBuild, Duration?>
            get() = __Duration
        val Error: KProperty1<ModelBuild, Boolean?>
            get() = __Error
        val ErrorMessage: KProperty1<ModelBuild, String?>
            get() = __ErrorMessage
        val Date: KProperty1<ModelBuild, Instant?>
            get() = __Date}
}

class ModelBuild_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ModelBuild>?>) : KCollectionPropertyPath<T, ModelBuild?,
        ModelBuild_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val type: KPropertyPath<T, ModelBuildType?>
        get() = KPropertyPath(this,__Type)

    val intentId: KPropertyPath<T, Id<IntentDefinition>?>
        get() = KPropertyPath(this,__IntentId)

    val entityTypeName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__EntityTypeName)

    val nbSentences: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__NbSentences)

    val duration: KPropertyPath<T, Duration?>
        get() = KPropertyPath(this,__Duration)

    val error: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Error)

    val errorMessage: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ErrorMessage)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ModelBuild_<T> =
            ModelBuild_(this, customProperty(this, additionalPath))}

class ModelBuild_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        ModelBuild>?>) : KMapPropertyPath<T, K, ModelBuild?, ModelBuild_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val type: KPropertyPath<T, ModelBuildType?>
        get() = KPropertyPath(this,__Type)

    val intentId: KPropertyPath<T, Id<IntentDefinition>?>
        get() = KPropertyPath(this,__IntentId)

    val entityTypeName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__EntityTypeName)

    val nbSentences: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__NbSentences)

    val duration: KPropertyPath<T, Duration?>
        get() = KPropertyPath(this,__Duration)

    val error: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Error)

    val errorMessage: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ErrorMessage)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ModelBuild_<T> =
            ModelBuild_(this, customProperty(this, additionalPath))}
