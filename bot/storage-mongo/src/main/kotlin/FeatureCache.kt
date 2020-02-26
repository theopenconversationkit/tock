package ai.tock.bot.mongo.ai.tock.bot.mongo

import ai.tock.bot.mongo.Feature

internal interface FeatureCache {
    fun stateOf(key: String): Feature?

    fun setState(key: String, value: Feature)

    fun invalidate(key: String)
}