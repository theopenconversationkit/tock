package ai.tock.nlp.model.service.storage.mongo

import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __ApplicationName:
        KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol, String?>
    get() = NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::applicationName
private val __EngineType:
        KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol,
        NlpEngineType?>
    get() = NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::engineType
private val __Configuration:
        KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol,
        NlpApplicationConfiguration?>
    get() = NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::configuration
private val __Date: KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol,
        Instant?>
    get() = NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::date
internal class NlpApplicationConfigurationCol_<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol?>) :
        KPropertyPath<T,
        NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol?>(previous,property) {
    val applicationName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationName)

    val engineType: KPropertyPath<T, NlpEngineType?>
        get() = KPropertyPath(this,__EngineType)

    val configuration: KPropertyPath<T, NlpApplicationConfiguration?>
        get() = KPropertyPath(this,__Configuration)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    companion object {
        val ApplicationName:
                KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol,
                String?>
            get() = __ApplicationName
        val EngineType:
                KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol,
                NlpEngineType?>
            get() = __EngineType
        val Configuration:
                KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol,
                NlpApplicationConfiguration?>
            get() = __Configuration
        val Date: KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol,
                Instant?>
            get() = __Date}
}

internal class NlpApplicationConfigurationCol_Col<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*,
        Collection<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol>?>) :
        KCollectionPropertyPath<T,
        NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol?,
        NlpApplicationConfigurationCol_<T>>(previous,property) {
    val applicationName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationName)

    val engineType: KPropertyPath<T, NlpEngineType?>
        get() = KPropertyPath(this,__EngineType)

    val configuration: KPropertyPath<T, NlpApplicationConfiguration?>
        get() = KPropertyPath(this,__Configuration)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            NlpApplicationConfigurationCol_<T> = NlpApplicationConfigurationCol_(this,
            customProperty(this, additionalPath))}

internal class NlpApplicationConfigurationCol_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol>?>)
        : KMapPropertyPath<T, K,
        NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol?,
        NlpApplicationConfigurationCol_<T>>(previous,property) {
    val applicationName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationName)

    val engineType: KPropertyPath<T, NlpEngineType?>
        get() = KPropertyPath(this,__EngineType)

    val configuration: KPropertyPath<T, NlpApplicationConfiguration?>
        get() = KPropertyPath(this,__Configuration)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            NlpApplicationConfigurationCol_<T> = NlpApplicationConfigurationCol_(this,
            customProperty(this, additionalPath))}
