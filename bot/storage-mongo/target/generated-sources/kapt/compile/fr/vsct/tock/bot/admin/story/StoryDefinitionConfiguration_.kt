package fr.vsct.tock.bot.admin.story

import fr.vsct.tock.bot.admin.answer.AnswerConfiguration
import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.Intent_
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __StoryId: KProperty1<StoryDefinitionConfiguration, String?>
    get() = StoryDefinitionConfiguration::storyId
private val __BotId: KProperty1<StoryDefinitionConfiguration, String?>
    get() = StoryDefinitionConfiguration::botId
private val __Intent: KProperty1<StoryDefinitionConfiguration, Intent?>
    get() = StoryDefinitionConfiguration::intent
private val __CurrentType: KProperty1<StoryDefinitionConfiguration, AnswerConfigurationType?>
    get() = StoryDefinitionConfiguration::currentType
private val __Answers: KProperty1<StoryDefinitionConfiguration, List<AnswerConfiguration>?>
    get() = StoryDefinitionConfiguration::answers
private val __Version: KProperty1<StoryDefinitionConfiguration, Int?>
    get() = StoryDefinitionConfiguration::version
private val __Namespace: KProperty1<StoryDefinitionConfiguration, String?>
    get() = StoryDefinitionConfiguration::namespace
private val __MandatoryEntities: KProperty1<StoryDefinitionConfiguration,
        List<StoryDefinitionConfigurationMandatoryEntity>?>
    get() = StoryDefinitionConfiguration::mandatoryEntities
private val __Steps: KProperty1<StoryDefinitionConfiguration,
        List<StoryDefinitionConfigurationStep>?>
    get() = StoryDefinitionConfiguration::steps
private val __Name: KProperty1<StoryDefinitionConfiguration, String?>
    get() = StoryDefinitionConfiguration::name
private val __Category: KProperty1<StoryDefinitionConfiguration, String?>
    get() = StoryDefinitionConfiguration::category
private val __Description: KProperty1<StoryDefinitionConfiguration, String?>
    get() = StoryDefinitionConfiguration::description
private val ___id: KProperty1<StoryDefinitionConfiguration, Id<StoryDefinitionConfiguration>?>
    get() = StoryDefinitionConfiguration::_id
class StoryDefinitionConfiguration_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        StoryDefinitionConfiguration?>) : KPropertyPath<T,
        StoryDefinitionConfiguration?>(previous,property) {
    val storyId: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__StoryId)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__BotId)

    val intent: Intent_<T>
        get() = Intent_(this,StoryDefinitionConfiguration::intent)

    val currentType: KPropertyPath<T, AnswerConfigurationType?>
        get() = KPropertyPath<T, AnswerConfigurationType?>(this,__CurrentType)

    val answers: KCollectionSimplePropertyPath<T, AnswerConfiguration?>
        get() = KCollectionSimplePropertyPath<T,
                AnswerConfiguration?>(this,StoryDefinitionConfiguration::answers)

    val version: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__Version)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Namespace)

    val mandatoryEntities: KCollectionSimplePropertyPath<T,
            StoryDefinitionConfigurationMandatoryEntity?>
        get() = KCollectionSimplePropertyPath<T,
                StoryDefinitionConfigurationMandatoryEntity?>(this,StoryDefinitionConfiguration::mandatoryEntities)

    val steps: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationStep?>
        get() = KCollectionSimplePropertyPath<T,
                StoryDefinitionConfigurationStep?>(this,StoryDefinitionConfiguration::steps)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Name)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Category)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Description)

    val _id: KPropertyPath<T, Id<StoryDefinitionConfiguration>?>
        get() = KPropertyPath<T, Id<StoryDefinitionConfiguration>?>(this,___id)

    companion object {
        val StoryId: KProperty1<StoryDefinitionConfiguration, String?>
            get() = __StoryId
        val BotId: KProperty1<StoryDefinitionConfiguration, String?>
            get() = __BotId
        val Intent: Intent_<StoryDefinitionConfiguration>
            get() = Intent_<StoryDefinitionConfiguration>(null,__Intent)
        val CurrentType: KProperty1<StoryDefinitionConfiguration, AnswerConfigurationType?>
            get() = __CurrentType
        val Answers: KCollectionSimplePropertyPath<StoryDefinitionConfiguration,
                AnswerConfiguration?>
            get() = KCollectionSimplePropertyPath(null, __Answers)
        val Version: KProperty1<StoryDefinitionConfiguration, Int?>
            get() = __Version
        val Namespace: KProperty1<StoryDefinitionConfiguration, String?>
            get() = __Namespace
        val MandatoryEntities: KCollectionSimplePropertyPath<StoryDefinitionConfiguration,
                StoryDefinitionConfigurationMandatoryEntity?>
            get() = KCollectionSimplePropertyPath(null, __MandatoryEntities)
        val Steps: KCollectionSimplePropertyPath<StoryDefinitionConfiguration,
                StoryDefinitionConfigurationStep?>
            get() = KCollectionSimplePropertyPath(null, __Steps)
        val Name: KProperty1<StoryDefinitionConfiguration, String?>
            get() = __Name
        val Category: KProperty1<StoryDefinitionConfiguration, String?>
            get() = __Category
        val Description: KProperty1<StoryDefinitionConfiguration, String?>
            get() = __Description
        val _id: KProperty1<StoryDefinitionConfiguration, Id<StoryDefinitionConfiguration>?>
            get() = ___id}
}

class StoryDefinitionConfiguration_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<StoryDefinitionConfiguration>?>) : KCollectionPropertyPath<T,
        StoryDefinitionConfiguration?, StoryDefinitionConfiguration_<T>>(previous,property) {
    val storyId: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__StoryId)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__BotId)

    val intent: Intent_<T>
        get() = Intent_(this,StoryDefinitionConfiguration::intent)

    val currentType: KPropertyPath<T, AnswerConfigurationType?>
        get() = KPropertyPath<T, AnswerConfigurationType?>(this,__CurrentType)

    val answers: KCollectionSimplePropertyPath<T, AnswerConfiguration?>
        get() = KCollectionSimplePropertyPath<T,
                AnswerConfiguration?>(this,StoryDefinitionConfiguration::answers)

    val version: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__Version)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Namespace)

    val mandatoryEntities: KCollectionSimplePropertyPath<T,
            StoryDefinitionConfigurationMandatoryEntity?>
        get() = KCollectionSimplePropertyPath<T,
                StoryDefinitionConfigurationMandatoryEntity?>(this,StoryDefinitionConfiguration::mandatoryEntities)

    val steps: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationStep?>
        get() = KCollectionSimplePropertyPath<T,
                StoryDefinitionConfigurationStep?>(this,StoryDefinitionConfiguration::steps)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Name)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Category)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Description)

    val _id: KPropertyPath<T, Id<StoryDefinitionConfiguration>?>
        get() = KPropertyPath<T, Id<StoryDefinitionConfiguration>?>(this,___id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): StoryDefinitionConfiguration_<T>
            = StoryDefinitionConfiguration_(this, customProperty(this, additionalPath))}

class StoryDefinitionConfiguration_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, StoryDefinitionConfiguration>?>) : KMapPropertyPath<T, K,
        StoryDefinitionConfiguration?, StoryDefinitionConfiguration_<T>>(previous,property) {
    val storyId: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__StoryId)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__BotId)

    val intent: Intent_<T>
        get() = Intent_(this,StoryDefinitionConfiguration::intent)

    val currentType: KPropertyPath<T, AnswerConfigurationType?>
        get() = KPropertyPath<T, AnswerConfigurationType?>(this,__CurrentType)

    val answers: KCollectionSimplePropertyPath<T, AnswerConfiguration?>
        get() = KCollectionSimplePropertyPath<T,
                AnswerConfiguration?>(this,StoryDefinitionConfiguration::answers)

    val version: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__Version)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Namespace)

    val mandatoryEntities: KCollectionSimplePropertyPath<T,
            StoryDefinitionConfigurationMandatoryEntity?>
        get() = KCollectionSimplePropertyPath<T,
                StoryDefinitionConfigurationMandatoryEntity?>(this,StoryDefinitionConfiguration::mandatoryEntities)

    val steps: KCollectionSimplePropertyPath<T, StoryDefinitionConfigurationStep?>
        get() = KCollectionSimplePropertyPath<T,
                StoryDefinitionConfigurationStep?>(this,StoryDefinitionConfiguration::steps)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Name)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Category)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Description)

    val _id: KPropertyPath<T, Id<StoryDefinitionConfiguration>?>
        get() = KPropertyPath<T, Id<StoryDefinitionConfiguration>?>(this,___id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): StoryDefinitionConfiguration_<T>
            = StoryDefinitionConfiguration_(this, customProperty(this, additionalPath))}
