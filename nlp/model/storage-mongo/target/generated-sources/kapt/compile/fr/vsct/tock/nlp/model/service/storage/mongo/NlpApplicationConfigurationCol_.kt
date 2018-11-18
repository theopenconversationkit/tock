package fr.vsct.tock.nlp.model.service.storage.mongo

import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.core.configuration.NlpApplicationConfiguration
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class NlpApplicationConfigurationCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol?>) : KPropertyPath<T, NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol?>(previous,property) {
    val applicationName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::applicationName)

    val engineType: KPropertyPath<T, NlpEngineType?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.nlp.core.NlpEngineType?>(this,NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::engineType)

    val configuration: KPropertyPath<T, NlpApplicationConfiguration?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.nlp.core.configuration.NlpApplicationConfiguration?>(this,NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::configuration)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::date)
    companion object {
        val ApplicationName: KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol, String?>
            get() = NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::applicationName
        val EngineType: KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol, NlpEngineType?>
            get() = NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::engineType
        val Configuration: KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol, NlpApplicationConfiguration?>
            get() = NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::configuration
        val Date: KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol, Instant?>
            get() = NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::date}
}

internal class NlpApplicationConfigurationCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol>?>) : KCollectionPropertyPath<T, NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol?, NlpApplicationConfigurationCol_<T>>(previous,property) {
    val applicationName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::applicationName)

    val engineType: KPropertyPath<T, NlpEngineType?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.nlp.core.NlpEngineType?>(this,NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::engineType)

    val configuration: KPropertyPath<T, NlpApplicationConfiguration?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.nlp.core.configuration.NlpApplicationConfiguration?>(this,NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::configuration)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): NlpApplicationConfigurationCol_<T> = NlpApplicationConfigurationCol_(this, customProperty(this, additionalPath))}

internal class NlpApplicationConfigurationCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol>?>) : KMapPropertyPath<T, K, NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol?, NlpApplicationConfigurationCol_<T>>(previous,property) {
    val applicationName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::applicationName)

    val engineType: KPropertyPath<T, NlpEngineType?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.nlp.core.NlpEngineType?>(this,NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::engineType)

    val configuration: KPropertyPath<T, NlpApplicationConfiguration?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.nlp.core.configuration.NlpApplicationConfiguration?>(this,NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::configuration)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): NlpApplicationConfigurationCol_<T> = NlpApplicationConfigurationCol_(this, customProperty(this, additionalPath))}
