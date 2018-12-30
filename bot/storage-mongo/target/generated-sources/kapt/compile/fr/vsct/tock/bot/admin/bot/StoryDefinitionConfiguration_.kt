package fr.vsct.tock.bot.admin.bot

import fr.vsct.tock.bot.admin.answer.AnswerConfiguration
import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType
import fr.vsct.tock.bot.definition.Intent_
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

class StoryDefinitionConfiguration_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        StoryDefinitionConfiguration?>) : KPropertyPath<T,
        StoryDefinitionConfiguration?>(previous,property) {
    val storyId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,StoryDefinitionConfiguration::storyId)

    val botId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,StoryDefinitionConfiguration::botId)

    val intent: Intent_<T>
        get() = Intent_(this,StoryDefinitionConfiguration::intent)

    val currentType: KPropertyPath<T, AnswerConfigurationType?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                fr.vsct.tock.bot.admin.answer.AnswerConfigurationType?>(this,StoryDefinitionConfiguration::currentType)

    val answers: KCollectionSimplePropertyPath<T, AnswerConfiguration?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                fr.vsct.tock.bot.admin.answer.AnswerConfiguration?>(this,StoryDefinitionConfiguration::answers)

    val version: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,StoryDefinitionConfiguration::version)

    val _id: KPropertyPath<T, Id<StoryDefinitionConfiguration>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.bot.admin.bot.StoryDefinitionConfiguration>?>(this,StoryDefinitionConfiguration::_id)

    companion object {
        val StoryId: KProperty1<StoryDefinitionConfiguration, String?>
            get() = StoryDefinitionConfiguration::storyId
        val BotId: KProperty1<StoryDefinitionConfiguration, String?>
            get() = StoryDefinitionConfiguration::botId
        val Intent: Intent_<StoryDefinitionConfiguration>
            get() = Intent_<StoryDefinitionConfiguration>(null,StoryDefinitionConfiguration::intent)
        val CurrentType: KProperty1<StoryDefinitionConfiguration, AnswerConfigurationType?>
            get() = StoryDefinitionConfiguration::currentType
        val Answers: KCollectionSimplePropertyPath<StoryDefinitionConfiguration,
                AnswerConfiguration?>
            get() = KCollectionSimplePropertyPath(null, StoryDefinitionConfiguration::answers)
        val Version: KProperty1<StoryDefinitionConfiguration, Int?>
            get() = StoryDefinitionConfiguration::version
        val _id: KProperty1<StoryDefinitionConfiguration, Id<StoryDefinitionConfiguration>?>
            get() = StoryDefinitionConfiguration::_id}
}

class StoryDefinitionConfiguration_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<StoryDefinitionConfiguration>?>) : KCollectionPropertyPath<T,
        StoryDefinitionConfiguration?, StoryDefinitionConfiguration_<T>>(previous,property) {
    val storyId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,StoryDefinitionConfiguration::storyId)

    val botId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,StoryDefinitionConfiguration::botId)

    val intent: Intent_<T>
        get() = Intent_(this,StoryDefinitionConfiguration::intent)

    val currentType: KPropertyPath<T, AnswerConfigurationType?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                fr.vsct.tock.bot.admin.answer.AnswerConfigurationType?>(this,StoryDefinitionConfiguration::currentType)

    val answers: KCollectionSimplePropertyPath<T, AnswerConfiguration?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                fr.vsct.tock.bot.admin.answer.AnswerConfiguration?>(this,StoryDefinitionConfiguration::answers)

    val version: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,StoryDefinitionConfiguration::version)

    val _id: KPropertyPath<T, Id<StoryDefinitionConfiguration>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.bot.admin.bot.StoryDefinitionConfiguration>?>(this,StoryDefinitionConfiguration::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): StoryDefinitionConfiguration_<T>
            = StoryDefinitionConfiguration_(this, customProperty(this, additionalPath))}

class StoryDefinitionConfiguration_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, StoryDefinitionConfiguration>?>) : KMapPropertyPath<T, K,
        StoryDefinitionConfiguration?, StoryDefinitionConfiguration_<T>>(previous,property) {
    val storyId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,StoryDefinitionConfiguration::storyId)

    val botId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,StoryDefinitionConfiguration::botId)

    val intent: Intent_<T>
        get() = Intent_(this,StoryDefinitionConfiguration::intent)

    val currentType: KPropertyPath<T, AnswerConfigurationType?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                fr.vsct.tock.bot.admin.answer.AnswerConfigurationType?>(this,StoryDefinitionConfiguration::currentType)

    val answers: KCollectionSimplePropertyPath<T, AnswerConfiguration?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                fr.vsct.tock.bot.admin.answer.AnswerConfiguration?>(this,StoryDefinitionConfiguration::answers)

    val version: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,StoryDefinitionConfiguration::version)

    val _id: KPropertyPath<T, Id<StoryDefinitionConfiguration>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.bot.admin.bot.StoryDefinitionConfiguration>?>(this,StoryDefinitionConfiguration::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): StoryDefinitionConfiguration_<T>
            = StoryDefinitionConfiguration_(this, customProperty(this, additionalPath))}
