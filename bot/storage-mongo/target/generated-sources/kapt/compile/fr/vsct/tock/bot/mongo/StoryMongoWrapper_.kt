package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.definition.Intent_
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class StoryMongoWrapper_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, DialogCol.StoryMongoWrapper?>) : KPropertyPath<T, DialogCol.StoryMongoWrapper?>(previous,property) {
    val storyDefinitionId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogCol.StoryMongoWrapper::storyDefinitionId)

    val currentIntent: Intent_<T>
        get() = Intent_(this,DialogCol.StoryMongoWrapper::currentIntent)

    val currentStep: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogCol.StoryMongoWrapper::currentStep)

    val actions: KProperty1<T, List<DialogCol.ActionMongoWrapper>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogCol.StoryMongoWrapper::actions)
    companion object {
        val StoryDefinitionId: KProperty1<DialogCol.StoryMongoWrapper, String?>
            get() = DialogCol.StoryMongoWrapper::storyDefinitionId
        val CurrentIntent: Intent_<DialogCol.StoryMongoWrapper>
            get() = Intent_<DialogCol.StoryMongoWrapper>(null,DialogCol.StoryMongoWrapper::currentIntent)
        val CurrentStep: KProperty1<DialogCol.StoryMongoWrapper, String?>
            get() = DialogCol.StoryMongoWrapper::currentStep
        val Actions: KProperty1<DialogCol.StoryMongoWrapper, List<DialogCol.ActionMongoWrapper>?>
            get() = DialogCol.StoryMongoWrapper::actions}
}

internal class StoryMongoWrapper_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<DialogCol.StoryMongoWrapper>?>) : KCollectionPropertyPath<T, DialogCol.StoryMongoWrapper?, StoryMongoWrapper_<T>>(previous,property) {
    val storyDefinitionId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogCol.StoryMongoWrapper::storyDefinitionId)

    val currentIntent: Intent_<T>
        get() = Intent_(this,DialogCol.StoryMongoWrapper::currentIntent)

    val currentStep: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogCol.StoryMongoWrapper::currentStep)

    val actions: KProperty1<T, List<DialogCol.ActionMongoWrapper>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogCol.StoryMongoWrapper::actions)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): StoryMongoWrapper_<T> = StoryMongoWrapper_(this, customProperty(this, additionalPath))}
