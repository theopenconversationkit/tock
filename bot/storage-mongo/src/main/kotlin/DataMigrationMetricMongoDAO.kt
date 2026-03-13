import ai.tock.bot.admin.indicators.Indicator
import ai.tock.bot.admin.indicators.IndicatorType
import ai.tock.bot.mongo.MongoBotConfiguration.database
import org.litote.kmongo.and
import org.litote.kmongo.exists
import org.litote.kmongo.getCollectionOfName
import org.litote.kmongo.setTo
import org.litote.kmongo.updateMany

/**
 * DAO object for migrating metrics and indicators in MongoDB.
 */
object DataMigrationMetricMongoDAO {
    internal val indicatorCol = database.getCollectionOfName<Indicator>("indicator")

    /**
     * Update all Indicators with type = CUSTOM
     */
    fun updateAllIndicatorsType() =
        indicatorCol.updateMany(
            and(
                Indicator::type exists false,
            ),
            Indicator::type setTo IndicatorType.CUSTOM,
        )
}
