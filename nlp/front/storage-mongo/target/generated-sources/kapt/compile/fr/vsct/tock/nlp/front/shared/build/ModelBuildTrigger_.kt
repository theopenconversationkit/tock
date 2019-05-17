package fr.vsct.tock.nlp.front.shared.build

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __ApplicationId: KProperty1<ModelBuildTrigger, Id<ApplicationDefinition>?>
    get() = ModelBuildTrigger::applicationId
private val __All: KProperty1<ModelBuildTrigger, Boolean?>
    get() = ModelBuildTrigger::all
private val __OnlyIfModelNotExists: KProperty1<ModelBuildTrigger, Boolean?>
    get() = ModelBuildTrigger::onlyIfModelNotExists
class ModelBuildTrigger_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ModelBuildTrigger?>) : KPropertyPath<T, ModelBuildTrigger?>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val all: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__All)

    val onlyIfModelNotExists: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__OnlyIfModelNotExists)

    companion object {
        val ApplicationId: KProperty1<ModelBuildTrigger, Id<ApplicationDefinition>?>
            get() = __ApplicationId
        val All: KProperty1<ModelBuildTrigger, Boolean?>
            get() = __All
        val OnlyIfModelNotExists: KProperty1<ModelBuildTrigger, Boolean?>
            get() = __OnlyIfModelNotExists}
}

class ModelBuildTrigger_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ModelBuildTrigger>?>) : KCollectionPropertyPath<T, ModelBuildTrigger?,
        ModelBuildTrigger_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val all: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__All)

    val onlyIfModelNotExists: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__OnlyIfModelNotExists)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ModelBuildTrigger_<T> =
            ModelBuildTrigger_(this, customProperty(this, additionalPath))}

class ModelBuildTrigger_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        ModelBuildTrigger>?>) : KMapPropertyPath<T, K, ModelBuildTrigger?,
        ModelBuildTrigger_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val all: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__All)

    val onlyIfModelNotExists: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__OnlyIfModelNotExists)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ModelBuildTrigger_<T> =
            ModelBuildTrigger_(this, customProperty(this, additionalPath))}
