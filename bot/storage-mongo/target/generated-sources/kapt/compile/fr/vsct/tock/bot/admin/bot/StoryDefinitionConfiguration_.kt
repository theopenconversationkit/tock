package fr.vsct.tock.bot.admin.bot

import fr.vsct.tock.bot.admin.answer.AnswerConfiguration
import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType
import fr.vsct.tock.bot.definition.Intent_
import kotlin.Int
import kotlin.String
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KPropertyPath

class StoryDefinitionConfiguration_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, StoryDefinitionConfiguration?>) : KPropertyPath<T, StoryDefinitionConfiguration?>(previous,property) {
    val storyId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,StoryDefinitionConfiguration::storyId)

    val botId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,StoryDefinitionConfiguration::botId)

    val intent: Intent_<T>
        get() = Intent_(this,StoryDefinitionConfiguration::intent)

    val currentType: KProperty1<T, AnswerConfigurationType?>
        get() = org.litote.kmongo.property.KPropertyPath(this,StoryDefinitionConfiguration::currentType)

    val answers: KProperty1<T, List<AnswerConfiguration>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,StoryDefinitionConfiguration::answers)

    val version: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,StoryDefinitionConfiguration::version)

    val _id: KProperty1<T, Id<StoryDefinitionConfiguration>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,StoryDefinitionConfiguration::_id)
    companion object {
        val StoryId: KProperty1<StoryDefinitionConfiguration, String?>
            get() = StoryDefinitionConfiguration::storyId
        val BotId: KProperty1<StoryDefinitionConfiguration, String?>
            get() = StoryDefinitionConfiguration::botId
        val Intent: Intent_<StoryDefinitionConfiguration>
            get() = Intent_<StoryDefinitionConfiguration>(null,StoryDefinitionConfiguration::intent)
        val CurrentType: KProperty1<StoryDefinitionConfiguration, AnswerConfigurationType?>
            get() = StoryDefinitionConfiguration::currentType
        val Answers: KProperty1<StoryDefinitionConfiguration, List<AnswerConfiguration>?>
            get() = StoryDefinitionConfiguration::answers
        val Version: KProperty1<StoryDefinitionConfiguration, Int?>
            get() = StoryDefinitionConfiguration::version
        val _id: KProperty1<StoryDefinitionConfiguration, Id<StoryDefinitionConfiguration>?>
            get() = StoryDefinitionConfiguration::_id}
}

class StoryDefinitionConfiguration_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<StoryDefinitionConfiguration>?>) : KPropertyPath<T, Collection<StoryDefinitionConfiguration>?>(previous,property) {
    val storyId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,StoryDefinitionConfiguration::storyId)

    val botId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,StoryDefinitionConfiguration::botId)

    val intent: Intent_<T>
        get() = Intent_(this,StoryDefinitionConfiguration::intent)

    val currentType: KProperty1<T, AnswerConfigurationType?>
        get() = org.litote.kmongo.property.KPropertyPath(this,StoryDefinitionConfiguration::currentType)

    val answers: KProperty1<T, List<AnswerConfiguration>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,StoryDefinitionConfiguration::answers)

    val version: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,StoryDefinitionConfiguration::version)

    val _id: KProperty1<T, Id<StoryDefinitionConfiguration>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,StoryDefinitionConfiguration::_id)
}
