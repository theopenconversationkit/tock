/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.nlp.model.service.storage.s3

import ai.tock.aws.s3.S3Bucket
import ai.tock.nlp.model.service.storage.NlpApplicationConfigurationDAO
import ai.tock.nlp.model.service.storage.NlpEngineModelDAO
import ai.tock.shared.Executor
import ai.tock.shared.injector
import ai.tock.shared.longProperty
import ai.tock.shared.property
import ai.tock.shared.propertyOrNull
import ai.tock.shared.provide
import ai.tock.shared.service.BotAdditionalModulesService
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import mu.KotlinLogging
import java.time.Duration

private val logger = KotlinLogging.logger {}

/**
 * Set to `s3` to store the model files in S3 instead of Mongo GridFS.
 */
private const val MODEL_STORAGE_PROPERTY = "tock_nlp_model_storage"

/**
 * Binds an [NlpEngineModelDAO] that stores the model files in S3, configured with:
 * - `tock_nlp_model_s3_bucket`: the bucket (required)
 * - `tock_nlp_model_s3_prefix`: the prefix of the keys, `tock/models/` by default
 * - `tock_nlp_model_s3_refresh_interval_seconds`: the interval between two checks of the model changes, 60 by default
 *
 * The S3 client is configured by the `tock_s3_*` properties, see [ai.tock.aws.s3.S3Clients].
 */
val modelS3Module =
    Kodein.Module(allowSilentOverride = true) {
        bind<NlpEngineModelDAO>() with
            singleton {
                val bucket =
                    S3Bucket(
                        checkNotNull(propertyOrNull("tock_nlp_model_s3_bucket")) { "tock_nlp_model_s3_bucket is required to store the models in S3" },
                        property("tock_nlp_model_s3_prefix", "tock/models/"),
                    )
                logger.info { "NLP models stored in s3://${bucket.bucket}/${bucket.prefix}" }
                S3NlpEngineModelDAO(
                    bucket,
                    Duration.ofSeconds(longProperty("tock_nlp_model_s3_refresh_interval_seconds", 60)),
                    { injector.provide<NlpApplicationConfigurationDAO>() },
                    { interval, task -> injector.provide<Executor>().setPeriodic("nlp_model_s3_poll", interval, interval, task) },
                )
            }
    }

/**
 * Overrides the Mongo storage of the models when `tock_nlp_model_storage` is `s3`.
 * Declared in META-INF/services/ai.tock.shared.service.BotAdditionalModulesService.
 */
class S3ModelModulesService : BotAdditionalModulesService {
    override fun customModules(): Set<Kodein.Module> = if (propertyOrNull(MODEL_STORAGE_PROPERTY).equals("s3", ignoreCase = true)) setOf(modelS3Module) else emptySet()
}
