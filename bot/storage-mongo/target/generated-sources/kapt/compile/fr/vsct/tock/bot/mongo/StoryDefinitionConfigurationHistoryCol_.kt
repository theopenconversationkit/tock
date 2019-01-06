package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.admin.bot.StoryDefinitionConfiguration
import fr.vsct.tock.bot.admin.bot.StoryDefinitionConfiguration_
import java.time.Instant
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Conf:
        KProperty1<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol,
        StoryDefinitionConfiguration?>
    get() = StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::conf
private val __Deleted:
        KProperty1<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol,
        Boolean?>
    get() = StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::deleted
private val __Date:
        KProperty1<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol,
        Instant?>
    get() = StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::date
internal class StoryDefinitionConfigurationHistoryCol_<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol?>)
        : KPropertyPath<T,
        StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol?>(previous,property)
        {
    val conf: StoryDefinitionConfiguration_<T>
        get() =
                StoryDefinitionConfiguration_(this,StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::conf)

    val deleted: KPropertyPath<T, Boolean?>
        get() = KPropertyPath<T, Boolean?>(this,__Deleted)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath<T, Instant?>(this,__Date)

    companion object {
        val Conf:
                StoryDefinitionConfiguration_<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol>
            get() =
                    StoryDefinitionConfiguration_<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol>(null,__Conf)
        val Deleted:
                KProperty1<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol,
                Boolean?>
            get() = __Deleted
        val Date:
                KProperty1<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol,
                Instant?>
            get() = __Date}
}

internal class StoryDefinitionConfigurationHistoryCol_Col<T>(previous: KPropertyPath<T, *>?,
        property: KProperty1<*,
        Collection<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol>?>) :
        KCollectionPropertyPath<T,
        StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol?,
        StoryDefinitionConfigurationHistoryCol_<T>>(previous,property) {
    val conf: StoryDefinitionConfiguration_<T>
        get() =
                StoryDefinitionConfiguration_(this,StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::conf)

    val deleted: KPropertyPath<T, Boolean?>
        get() = KPropertyPath<T, Boolean?>(this,__Deleted)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath<T, Instant?>(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            StoryDefinitionConfigurationHistoryCol_<T> =
            StoryDefinitionConfigurationHistoryCol_(this, customProperty(this, additionalPath))}

internal class StoryDefinitionConfigurationHistoryCol_Map<T, K>(previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Map<K,
        StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol>?>) :
        KMapPropertyPath<T, K,
        StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol?,
        StoryDefinitionConfigurationHistoryCol_<T>>(previous,property) {
    val conf: StoryDefinitionConfiguration_<T>
        get() =
                StoryDefinitionConfiguration_(this,StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::conf)

    val deleted: KPropertyPath<T, Boolean?>
        get() = KPropertyPath<T, Boolean?>(this,__Deleted)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath<T, Instant?>(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            StoryDefinitionConfigurationHistoryCol_<T> =
            StoryDefinitionConfigurationHistoryCol_(this, customProperty(this, additionalPath))}
