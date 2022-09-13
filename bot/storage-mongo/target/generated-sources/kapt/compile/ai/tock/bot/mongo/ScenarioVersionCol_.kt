package ai.tock.bot.mongo

import ai.tock.bot.admin.scenario.Scenario
import ai.tock.bot.admin.scenario.ScenarioState
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

private val __Version: KProperty1<ScenarioVersionCol, Id<Scenario>?>
    get() = ScenarioVersionCol::version
private val __Name: KProperty1<ScenarioVersionCol, String?>
    get() = ScenarioVersionCol::name
private val __Category: KProperty1<ScenarioVersionCol, String?>
    get() = ScenarioVersionCol::category
private val __Tags: KProperty1<ScenarioVersionCol, List<String>?>
    get() = ScenarioVersionCol::tags
private val __ApplicationId: KProperty1<ScenarioVersionCol, String?>
    get() = ScenarioVersionCol::applicationId
private val __CreationDate: KProperty1<ScenarioVersionCol, ZonedDateTime?>
    get() = ScenarioVersionCol::creationDate
private val __UpdateDate: KProperty1<ScenarioVersionCol, ZonedDateTime?>
    get() = ScenarioVersionCol::updateDate
private val __Description: KProperty1<ScenarioVersionCol, String?>
    get() = ScenarioVersionCol::description
private val __Data: KProperty1<ScenarioVersionCol, String?>
    get() = ScenarioVersionCol::data
private val __State: KProperty1<ScenarioVersionCol, ScenarioState?>
    get() = ScenarioVersionCol::state
internal class ScenarioVersionCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ScenarioVersionCol?>) : KPropertyPath<T, ScenarioVersionCol?>(previous,property) {
    val version: KPropertyPath<T, Id<Scenario>?>
        get() = KPropertyPath(this,__Version)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Category)

    val tags: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,ScenarioVersionCol::tags)

    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    val creationDate: KPropertyPath<T, ZonedDateTime?>
        get() = KPropertyPath(this,__CreationDate)

    val updateDate: KPropertyPath<T, ZonedDateTime?>
        get() = KPropertyPath(this,__UpdateDate)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val data: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Data)

    val state: KPropertyPath<T, ScenarioState?>
        get() = KPropertyPath(this,__State)

    companion object {
        val Version: KProperty1<ScenarioVersionCol, Id<Scenario>?>
            get() = __Version
        val Name: KProperty1<ScenarioVersionCol, String?>
            get() = __Name
        val Category: KProperty1<ScenarioVersionCol, String?>
            get() = __Category
        val Tags: KCollectionSimplePropertyPath<ScenarioVersionCol, String?>
            get() = KCollectionSimplePropertyPath(null, __Tags)
        val ApplicationId: KProperty1<ScenarioVersionCol, String?>
            get() = __ApplicationId
        val CreationDate: KProperty1<ScenarioVersionCol, ZonedDateTime?>
            get() = __CreationDate
        val UpdateDate: KProperty1<ScenarioVersionCol, ZonedDateTime?>
            get() = __UpdateDate
        val Description: KProperty1<ScenarioVersionCol, String?>
            get() = __Description
        val Data: KProperty1<ScenarioVersionCol, String?>
            get() = __Data
        val State: KProperty1<ScenarioVersionCol, ScenarioState?>
            get() = __State}
}

internal class ScenarioVersionCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ScenarioVersionCol>?>) : KCollectionPropertyPath<T, ScenarioVersionCol?,
        ScenarioVersionCol_<T>>(previous,property) {
    val version: KPropertyPath<T, Id<Scenario>?>
        get() = KPropertyPath(this,__Version)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Category)

    val tags: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,ScenarioVersionCol::tags)

    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    val creationDate: KPropertyPath<T, ZonedDateTime?>
        get() = KPropertyPath(this,__CreationDate)

    val updateDate: KPropertyPath<T, ZonedDateTime?>
        get() = KPropertyPath(this,__UpdateDate)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val data: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Data)

    val state: KPropertyPath<T, ScenarioState?>
        get() = KPropertyPath(this,__State)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ScenarioVersionCol_<T> =
            ScenarioVersionCol_(this, customProperty(this, additionalPath))}

internal class ScenarioVersionCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, ScenarioVersionCol>?>) : KMapPropertyPath<T, K, ScenarioVersionCol?,
        ScenarioVersionCol_<T>>(previous,property) {
    val version: KPropertyPath<T, Id<Scenario>?>
        get() = KPropertyPath(this,__Version)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Category)

    val tags: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,ScenarioVersionCol::tags)

    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    val creationDate: KPropertyPath<T, ZonedDateTime?>
        get() = KPropertyPath(this,__CreationDate)

    val updateDate: KPropertyPath<T, ZonedDateTime?>
        get() = KPropertyPath(this,__UpdateDate)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val data: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Data)

    val state: KPropertyPath<T, ScenarioState?>
        get() = KPropertyPath(this,__State)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ScenarioVersionCol_<T> =
            ScenarioVersionCol_(this, customProperty(this, additionalPath))}
