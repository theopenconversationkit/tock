package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.admin.bot.StoryDefinitionConfiguration_
import java.time.Instant
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class StoryDefinitionConfigurationHistoryCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol?>) : KPropertyPath<T, StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol?>(previous,property) {
    val conf: StoryDefinitionConfiguration_<T>
        get() = StoryDefinitionConfiguration_(this,StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::conf)

    val deleted: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::deleted)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::date)
    companion object {
        val Conf: StoryDefinitionConfiguration_<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol>
            get() = StoryDefinitionConfiguration_<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol>(null,StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::conf)
        val Deleted: KProperty1<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol, Boolean?>
            get() = StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::deleted
        val Date: KProperty1<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol, Instant?>
            get() = StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::date}
}

internal class StoryDefinitionConfigurationHistoryCol_Col<T>(
        previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Collection<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol>?>,
        additionalPath: String? = null
) : KCollectionPropertyPath<T, StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol?>(previous,property,additionalPath) {
    override val arrayProjection: StoryDefinitionConfigurationHistoryCol_Col<T>
        @Suppress("UNCHECKED_CAST")
        get() = StoryDefinitionConfigurationHistoryCol_Col(null, this as KProperty1<*, Collection<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol>?>, "$")

    val conf: StoryDefinitionConfiguration_<T>
        get() = StoryDefinitionConfiguration_(this,StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::conf)

    val deleted: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::deleted)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::date)
}
