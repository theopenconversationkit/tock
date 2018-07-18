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
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

class ModelBuild_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ModelBuild?>) : KPropertyPath<T, ModelBuild?>(previous,property) {
    val applicationId: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::applicationId)

    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::language)

    val type: KProperty1<T, ModelBuildType?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::type)

    val intentId: KProperty1<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::intentId)

    val entityTypeName: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::entityTypeName)

    val nbSentences: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::nbSentences)

    val duration: KProperty1<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::duration)

    val error: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::error)

    val errorMessage: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::errorMessage)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::date)
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

class ModelBuild_Col<T>(
        previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Collection<ModelBuild>?>,
        additionalPath: String? = null
) : KCollectionPropertyPath<T, ModelBuild?>(previous,property,additionalPath) {
    override val arrayProjection: ModelBuild_Col<T>
        @Suppress("UNCHECKED_CAST")
        get() = ModelBuild_Col(null, this as KProperty1<*, Collection<ModelBuild>?>, "$")

    val applicationId: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::applicationId)

    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::language)

    val type: KProperty1<T, ModelBuildType?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::type)

    val intentId: KProperty1<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::intentId)

    val entityTypeName: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::entityTypeName)

    val nbSentences: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::nbSentences)

    val duration: KProperty1<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::duration)

    val error: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::error)

    val errorMessage: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::errorMessage)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ModelBuild::date)
}
