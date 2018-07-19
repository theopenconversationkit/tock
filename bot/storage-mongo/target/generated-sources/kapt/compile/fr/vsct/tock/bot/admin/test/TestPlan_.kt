package fr.vsct.tock.bot.admin.test

import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.message.Message
import java.util.Locale
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

class TestPlan_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, TestPlan?>) : KPropertyPath<T, TestPlan?>(previous,property) {
    val dialogs: KProperty1<T, List<TestDialogReport>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::dialogs)

    val name_: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::name)

    val applicationId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::applicationId)

    val namespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::namespace)

    val nlpModel: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::nlpModel)

    val botApplicationConfigurationId: KProperty1<T, Id<BotApplicationConfiguration>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::botApplicationConfigurationId)

    val locale: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::locale)

    val startAction: KProperty1<T, Message?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::startAction)

    val targetConnectorType: KProperty1<T, ConnectorType?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::targetConnectorType)

    val _id: KProperty1<T, Id<TestPlan>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::_id)
    companion object {
        val Dialogs: KProperty1<TestPlan, List<TestDialogReport>?>
            get() = TestPlan::dialogs
        val Name: KProperty1<TestPlan, String?>
            get() = TestPlan::name
        val ApplicationId: KProperty1<TestPlan, String?>
            get() = TestPlan::applicationId
        val Namespace: KProperty1<TestPlan, String?>
            get() = TestPlan::namespace
        val NlpModel: KProperty1<TestPlan, String?>
            get() = TestPlan::nlpModel
        val BotApplicationConfigurationId: KProperty1<TestPlan, Id<BotApplicationConfiguration>?>
            get() = TestPlan::botApplicationConfigurationId
        val Locale: KProperty1<TestPlan, Locale?>
            get() = TestPlan::locale
        val StartAction: KProperty1<TestPlan, Message?>
            get() = TestPlan::startAction
        val TargetConnectorType: KProperty1<TestPlan, ConnectorType?>
            get() = TestPlan::targetConnectorType
        val _id: KProperty1<TestPlan, Id<TestPlan>?>
            get() = TestPlan::_id}
}

class TestPlan_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<TestPlan>?>) : KCollectionPropertyPath<T, TestPlan?, TestPlan_<T>>(previous,property) {
    val dialogs: KProperty1<T, List<TestDialogReport>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::dialogs)

    val name_: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::name)

    val applicationId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::applicationId)

    val namespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::namespace)

    val nlpModel: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::nlpModel)

    val botApplicationConfigurationId: KProperty1<T, Id<BotApplicationConfiguration>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::botApplicationConfigurationId)

    val locale: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::locale)

    val startAction: KProperty1<T, Message?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::startAction)

    val targetConnectorType: KProperty1<T, ConnectorType?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::targetConnectorType)

    val _id: KProperty1<T, Id<TestPlan>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlan::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestPlan_<T> = TestPlan_(this, customProperty(this, additionalPath))}
