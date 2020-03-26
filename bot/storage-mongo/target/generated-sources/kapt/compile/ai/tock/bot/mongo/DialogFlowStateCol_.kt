package ai.tock.bot.mongo

import ai.tock.bot.admin.answer.AnswerConfigurationType
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Namespace: KProperty1<DialogFlowStateCol, String?>
    get() = DialogFlowStateCol::namespace
private val __BotId: KProperty1<DialogFlowStateCol, String?>
    get() = DialogFlowStateCol::botId
private val __StoryDefinitionId: KProperty1<DialogFlowStateCol, String?>
    get() = DialogFlowStateCol::storyDefinitionId
private val __Intent: KProperty1<DialogFlowStateCol, String?>
    get() = DialogFlowStateCol::intent
private val __Step: KProperty1<DialogFlowStateCol, String?>
    get() = DialogFlowStateCol::step
private val __Entities: KProperty1<DialogFlowStateCol, Set<String>?>
    get() = DialogFlowStateCol::entities
private val ___id: KProperty1<DialogFlowStateCol, Id<DialogFlowStateCol>?>
    get() = DialogFlowStateCol::_id
private val __StoryType: KProperty1<DialogFlowStateCol, AnswerConfigurationType?>
    get() = DialogFlowStateCol::storyType
private val __StoryName: KProperty1<DialogFlowStateCol, String?>
    get() = DialogFlowStateCol::storyName
internal class DialogFlowStateCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        DialogFlowStateCol?>) : KPropertyPath<T, DialogFlowStateCol?>(previous,property) {
    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BotId)

    val storyDefinitionId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryDefinitionId)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val step: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Step)

    val entities: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,DialogFlowStateCol::entities)

    val _id: KPropertyPath<T, Id<DialogFlowStateCol>?>
        get() = KPropertyPath(this,___id)

    val storyType: KPropertyPath<T, AnswerConfigurationType?>
        get() = KPropertyPath(this,__StoryType)

    val storyName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryName)

    companion object {
        val Namespace: KProperty1<DialogFlowStateCol, String?>
            get() = __Namespace
        val BotId: KProperty1<DialogFlowStateCol, String?>
            get() = __BotId
        val StoryDefinitionId: KProperty1<DialogFlowStateCol, String?>
            get() = __StoryDefinitionId
        val Intent: KProperty1<DialogFlowStateCol, String?>
            get() = __Intent
        val Step: KProperty1<DialogFlowStateCol, String?>
            get() = __Step
        val Entities: KCollectionSimplePropertyPath<DialogFlowStateCol, String?>
            get() = KCollectionSimplePropertyPath(null, __Entities)
        val _id: KProperty1<DialogFlowStateCol, Id<DialogFlowStateCol>?>
            get() = ___id
        val StoryType: KProperty1<DialogFlowStateCol, AnswerConfigurationType?>
            get() = __StoryType
        val StoryName: KProperty1<DialogFlowStateCol, String?>
            get() = __StoryName}
}

internal class DialogFlowStateCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<DialogFlowStateCol>?>) : KCollectionPropertyPath<T, DialogFlowStateCol?,
        DialogFlowStateCol_<T>>(previous,property) {
    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BotId)

    val storyDefinitionId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryDefinitionId)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val step: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Step)

    val entities: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,DialogFlowStateCol::entities)

    val _id: KPropertyPath<T, Id<DialogFlowStateCol>?>
        get() = KPropertyPath(this,___id)

    val storyType: KPropertyPath<T, AnswerConfigurationType?>
        get() = KPropertyPath(this,__StoryType)

    val storyName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryName)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DialogFlowStateCol_<T> =
            DialogFlowStateCol_(this, customProperty(this, additionalPath))}

internal class DialogFlowStateCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, DialogFlowStateCol>?>) : KMapPropertyPath<T, K, DialogFlowStateCol?,
        DialogFlowStateCol_<T>>(previous,property) {
    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BotId)

    val storyDefinitionId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryDefinitionId)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val step: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Step)

    val entities: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,DialogFlowStateCol::entities)

    val _id: KPropertyPath<T, Id<DialogFlowStateCol>?>
        get() = KPropertyPath(this,___id)

    val storyType: KPropertyPath<T, AnswerConfigurationType?>
        get() = KPropertyPath(this,__StoryType)

    val storyName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryName)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DialogFlowStateCol_<T> =
            DialogFlowStateCol_(this, customProperty(this, additionalPath))}
