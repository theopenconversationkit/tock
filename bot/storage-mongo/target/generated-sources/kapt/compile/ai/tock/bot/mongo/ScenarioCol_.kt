package ai.tock.bot.mongo

import ai.tock.bot.admin.scenario.Scenario
import java.time.ZonedDateTime
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

private val ___id: KProperty1<ScenarioCol, Id<Scenario>?>
    get() = ScenarioCol::_id
private val __Name: KProperty1<ScenarioCol, String?>
    get() = ScenarioCol::name
private val __Category: KProperty1<ScenarioCol, String?>
    get() = ScenarioCol::category
private val __Tags: KProperty1<ScenarioCol, List<String>?>
    get() = ScenarioCol::tags
private val __ApplicationId: KProperty1<ScenarioCol, String?>
    get() = ScenarioCol::applicationId
private val __CreateDate: KProperty1<ScenarioCol, ZonedDateTime?>
    get() = ScenarioCol::createDate
private val __UpdateDate: KProperty1<ScenarioCol, ZonedDateTime?>
    get() = ScenarioCol::updateDate
private val __Description: KProperty1<ScenarioCol, String?>
    get() = ScenarioCol::description
private val __Data: KProperty1<ScenarioCol, String?>
    get() = ScenarioCol::data
private val __State: KProperty1<ScenarioCol, String?>
    get() = ScenarioCol::state
internal class ScenarioCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ScenarioCol?>) : KPropertyPath<T, ScenarioCol?>(previous,property) {
    val _id: KPropertyPath<T, Id<Scenario>?>
        get() = KPropertyPath(this,___id)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Category)

    val tags: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,ScenarioCol::tags)

    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    val createDate: KPropertyPath<T, ZonedDateTime?>
        get() = KPropertyPath(this,__CreateDate)

    val updateDate: KPropertyPath<T, ZonedDateTime?>
        get() = KPropertyPath(this,__UpdateDate)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val data: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Data)

    val state: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__State)

    companion object {
        val _id: KProperty1<ScenarioCol, Id<Scenario>?>
            get() = ___id
        val Name: KProperty1<ScenarioCol, String?>
            get() = __Name
        val Category: KProperty1<ScenarioCol, String?>
            get() = __Category
        val Tags: KCollectionSimplePropertyPath<ScenarioCol, String?>
            get() = KCollectionSimplePropertyPath(null, __Tags)
        val ApplicationId: KProperty1<ScenarioCol, String?>
            get() = __ApplicationId
        val CreateDate: KProperty1<ScenarioCol, ZonedDateTime?>
            get() = __CreateDate
        val UpdateDate: KProperty1<ScenarioCol, ZonedDateTime?>
            get() = __UpdateDate
        val Description: KProperty1<ScenarioCol, String?>
            get() = __Description
        val Data: KProperty1<ScenarioCol, String?>
            get() = __Data
        val State: KProperty1<ScenarioCol, String?>
            get() = __State}
}

internal class ScenarioCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ScenarioCol>?>) : KCollectionPropertyPath<T, ScenarioCol?,
        ScenarioCol_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<Scenario>?>
        get() = KPropertyPath(this,___id)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Category)

    val tags: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,ScenarioCol::tags)

    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    val createDate: KPropertyPath<T, ZonedDateTime?>
        get() = KPropertyPath(this,__CreateDate)

    val updateDate: KPropertyPath<T, ZonedDateTime?>
        get() = KPropertyPath(this,__UpdateDate)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val data: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Data)

    val state: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__State)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ScenarioCol_<T> =
            ScenarioCol_(this, customProperty(this, additionalPath))}

internal class ScenarioCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        ScenarioCol>?>) : KMapPropertyPath<T, K, ScenarioCol?, ScenarioCol_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<Scenario>?>
        get() = KPropertyPath(this,___id)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Category)

    val tags: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,ScenarioCol::tags)

    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    val createDate: KPropertyPath<T, ZonedDateTime?>
        get() = KPropertyPath(this,__CreateDate)

    val updateDate: KPropertyPath<T, ZonedDateTime?>
        get() = KPropertyPath(this,__UpdateDate)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val data: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Data)

    val state: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__State)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ScenarioCol_<T> =
            ScenarioCol_(this, customProperty(this, additionalPath))}
