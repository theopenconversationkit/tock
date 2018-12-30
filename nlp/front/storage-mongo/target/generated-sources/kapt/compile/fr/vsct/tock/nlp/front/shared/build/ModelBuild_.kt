package fr.vsct.tock.nlp.front.shared.build

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
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

class ModelBuild_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ModelBuild?>) :
        KPropertyPath<T, ModelBuild?>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ModelBuild::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.util.Locale?>(this,ModelBuild::language)

    val type: KPropertyPath<T, ModelBuildType?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                fr.vsct.tock.nlp.front.shared.build.ModelBuildType?>(this,ModelBuild::type)

    val intentId: KPropertyPath<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?>(this,ModelBuild::intentId)

    val entityTypeName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,ModelBuild::entityTypeName)

    val nbSentences: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,ModelBuild::nbSentences)

    val duration: KPropertyPath<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Duration?>(this,ModelBuild::duration)

    val error: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,ModelBuild::error)

    val errorMessage: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,ModelBuild::errorMessage)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,ModelBuild::date)

    companion object {
        val ApplicationId: KProperty1<ModelBuild, Id<ApplicationDefinition>?>
            get() = ModelBuild::applicationId
        val Language: KProperty1<ModelBuild, Locale?>
            get() = ModelBuild::language
        val Type: KProperty1<ModelBuild, ModelBuildType?>
            get() = ModelBuild::type
        val IntentId: KProperty1<ModelBuild, Id<IntentDefinition>?>
            get() = ModelBuild::intentId
        val EntityTypeName: KProperty1<ModelBuild, String?>
            get() = ModelBuild::entityTypeName
        val NbSentences: KProperty1<ModelBuild, Int?>
            get() = ModelBuild::nbSentences
        val Duration: KProperty1<ModelBuild, Duration?>
            get() = ModelBuild::duration
        val Error: KProperty1<ModelBuild, Boolean?>
            get() = ModelBuild::error
        val ErrorMessage: KProperty1<ModelBuild, String?>
            get() = ModelBuild::errorMessage
        val Date: KProperty1<ModelBuild, Instant?>
            get() = ModelBuild::date}
}

class ModelBuild_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ModelBuild>?>) : KCollectionPropertyPath<T, ModelBuild?,
        ModelBuild_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ModelBuild::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.util.Locale?>(this,ModelBuild::language)

    val type: KPropertyPath<T, ModelBuildType?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                fr.vsct.tock.nlp.front.shared.build.ModelBuildType?>(this,ModelBuild::type)

    val intentId: KPropertyPath<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?>(this,ModelBuild::intentId)

    val entityTypeName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,ModelBuild::entityTypeName)

    val nbSentences: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,ModelBuild::nbSentences)

    val duration: KPropertyPath<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Duration?>(this,ModelBuild::duration)

    val error: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,ModelBuild::error)

    val errorMessage: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,ModelBuild::errorMessage)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,ModelBuild::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ModelBuild_<T> =
            ModelBuild_(this, customProperty(this, additionalPath))}

class ModelBuild_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        ModelBuild>?>) : KMapPropertyPath<T, K, ModelBuild?, ModelBuild_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ModelBuild::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.util.Locale?>(this,ModelBuild::language)

    val type: KPropertyPath<T, ModelBuildType?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                fr.vsct.tock.nlp.front.shared.build.ModelBuildType?>(this,ModelBuild::type)

    val intentId: KPropertyPath<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?>(this,ModelBuild::intentId)

    val entityTypeName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,ModelBuild::entityTypeName)

    val nbSentences: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,ModelBuild::nbSentences)

    val duration: KPropertyPath<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Duration?>(this,ModelBuild::duration)

    val error: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,ModelBuild::error)

    val errorMessage: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,ModelBuild::errorMessage)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,ModelBuild::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ModelBuild_<T> =
            ModelBuild_(this, customProperty(this, additionalPath))}
