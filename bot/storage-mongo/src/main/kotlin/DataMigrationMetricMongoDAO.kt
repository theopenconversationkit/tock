import ai.tock.bot.admin.indicators.Indicator
import ai.tock.bot.admin.indicators.metric.Metric
import ai.tock.bot.mongo.MongoBotConfiguration.database
import com.mongodb.client.model.*
import org.bson.Document
import org.litote.kmongo.*

/**
 * DAO object for migrating metrics and indicators in MongoDB.
 *
 * Provides functions to:
 *  - Count eligible metrics and indicators for migration.
 *  - Retrieve projections of metrics and bots for enrichment.
 *  - Perform bulk updates on metrics and indicators based on their projections.
 *
 * This object centralizes all MongoDB operations related to data migration for metrics
 * and indicators, ensuring consistency and safety checks (e.g., handling duplicates or missing fields).
 */
object DataMigrationMetricMongoDAO {
    internal val botCol = database.getCollectionOfName<Metric>("bot")
    internal val metricCol = database.getCollectionOfName<Metric>("metric")
    internal val indicatorCol = database.getCollectionOfName<Indicator>("indicator")

    /**
     * Counts metrics that are missing either the `namespace` or `applicationId` field.
     *
     * @return the number of metrics eligible for migration.
     */
    fun countEligibleMetrics(): Long = metricCol.countDocuments(
        or(
            Metric::namespace exists false,
            Metric::applicationId exists false
        )
    )

    /**
     * Counts indicators that are missing the `namespace` field.
     *
     * @return the number of indicators eligible for migration.
     */
    fun countEligibleIndicators(): Long = indicatorCol.countDocuments(
        Indicator::namespace exists false
    )

    /**
     * Retrieves all bots with their associated namespaces.
     *
     * Groups bots by `botId` and collects all namespaces for each bot.
     *
     * @return a list of [BotProjectionResult] containing each botId and its namespaces.
     */
    fun getBotProjections(): List<BotProjectionResult> {
        val pipeline = listOf(
            group(
                "\$botId",
                Accumulators.addToSet("namespaces", "\$namespace")
            ),
            project(
                Projections.computed("botId", "\$_id"),
                Projections.include("namespaces")
            )
        )

        return botCol.aggregate(pipeline, BotProjectionResult::class.java).toList()
    }

    /**
     * Set the namespace to indicators that do not have one
     */
    fun updateIndicatorByProjections(botProjection : List<BotProjectionResult>): Long {
        if (botProjection.isEmpty()) return 0L

        val updates = botProjection.map { projection ->
            UpdateOneModel<Indicator>(
                and(
                    Indicator::botId eq projection.botId,
                    Indicator::namespace exists false
                ),
                Updates.set("namespace", projection.namespaces.first()),
                UpdateOptions().upsert(false)
            )
        }

        val bulkResult = indicatorCol.bulkWrite(updates)
        return bulkResult.modifiedCount.toLong()
    }

    /**
     * Retrieves metrics that are missing `namespace` or `applicationId` and enriches them with
     * data from their associated dialogs.
     *
     * For each metric:
     *  - Looks up the related dialog.
     *  - Extracts the dialog's namespace.
     *  - Picks the `applicationId` of the first action of the first story in the dialog.
     *
     * @return a list of [MetricProjectionResult] containing metricId, dialogId, dialogNamespace, and dialogApplicationId.
     */
    fun getMetricProjections(): List<MetricProjectionResult> {
        val pipeline = listOf(
            match(or(
                Metric::namespace exists false,
                Metric::applicationId exists false,
                )
            ),
            lookup(from = "dialog", localField = "dialogId", foreignField = "_id", newAs = "dialogs"),
            unwind("\$dialogs", UnwindOptions().preserveNullAndEmptyArrays(true)),
            project(
                Projections.computed("metricId", "\$_id"),
                Projections.computed("dialogId", "\$dialogId"),
                Projections.computed("dialogNamespace", "\$dialogs.namespace"),
                // Pick first story's first action applicationId
                Projections.computed(
                    "dialogApplicationId",
                    Document(
                        "\$arrayElemAt", listOf(
                            Document(
                                "\$arrayElemAt", listOf("\$dialogs.stories.actions.applicationId", 0)
                            ),
                            0
                        )
                    )
                )
            )
        )

        return metricCol.aggregate(pipeline, MetricProjectionResult::class.java).toList()
    }

    /**
     * Updates all metrics where `namespace` or `applicationId` are missing,
     * @return number of modified documents.
     */
    fun updateMetricByProjections(metricProjections : List<MetricProjectionResult>): Long {
        if (metricProjections.isEmpty()) return 0L

        val updates = metricProjections.map { projection ->
            UpdateOneModel<Metric>(
                and(
                    Metric::_id eq projection.metricId.toId(),
                    Metric::namespace exists false,
                    Metric::applicationId exists false
                ),
                Updates.combine(
                    Updates.set("namespace", projection.dialogNamespace),
                    Updates.set("applicationId", projection.dialogApplicationId)
                ),
                UpdateOptions().upsert(false)
            )
        }

        val bulkResult = metricCol.bulkWrite(updates)
        return bulkResult.modifiedCount.toLong()
    }

}

data class BotProjectionResult(
    val botId: String,
    val namespaces: List<String>
)

data class MetricProjectionResult(
    val metricId: String,
    val dialogId: String,
    val dialogNamespace: String?,
    val dialogApplicationId: String?,
)