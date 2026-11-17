@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.model.service.storage.mongo

import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration_
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __ApplicationName:
    KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol, String?>
  get() = NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::applicationName

private val __Configuration:
    KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol, NlpApplicationConfiguration?>
  get() = NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::configuration

private val __Date:
    KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol, Instant?>
  get() = NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::date

private val __EngineType:
    KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol, NlpEngineType?>
  get() = NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::engineType

internal class NlpApplicationConfigurationCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol?>,
) : KPropertyPath<T, NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol?>(previous, property) {
  public val applicationName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ApplicationName)

  public val configuration: NlpApplicationConfiguration_<T>
    get() = NlpApplicationConfiguration_(this, __Configuration)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val engineType: KPropertyPath<T, NlpEngineType?>
    get() = KPropertyPath(this, __EngineType)

  public companion object {
    public val ApplicationName:
        KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol, String?>
      get() = __ApplicationName

    public val Configuration:
        NlpApplicationConfiguration_<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol>
      get() = NlpApplicationConfiguration_(null, __Configuration)

    public val Date:
        KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol, Instant?>
      get() = __Date

    public val EngineType:
        KProperty1<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol, NlpEngineType?>
      get() = __EngineType
  }
}

internal class NlpApplicationConfigurationCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol?>?>,
) : KCollectionPropertyPath<T, NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol?, NlpApplicationConfigurationCol_<T>>(previous, property) {
  public val applicationName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ApplicationName)

  public val configuration: NlpApplicationConfiguration_<T>
    get() = NlpApplicationConfiguration_(this, __Configuration)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val engineType: KPropertyPath<T, NlpEngineType?>
    get() = KPropertyPath(this, __EngineType)

  override fun memberWithAdditionalPath(additionalPath: String): NlpApplicationConfigurationCol_<T> = NlpApplicationConfigurationCol_(this, customProperty(this, additionalPath))
}

internal class NlpApplicationConfigurationCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol?>?>,
) : KMapPropertyPath<T, K, NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol?, NlpApplicationConfigurationCol_<T>>(previous, property) {
  public val applicationName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ApplicationName)

  public val configuration: NlpApplicationConfiguration_<T>
    get() = NlpApplicationConfiguration_(this, __Configuration)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val engineType: KPropertyPath<T, NlpEngineType?>
    get() = KPropertyPath(this, __EngineType)

  override fun memberWithAdditionalPath(additionalPath: String): NlpApplicationConfigurationCol_<T> = NlpApplicationConfigurationCol_(this, customProperty(this, additionalPath))
}
