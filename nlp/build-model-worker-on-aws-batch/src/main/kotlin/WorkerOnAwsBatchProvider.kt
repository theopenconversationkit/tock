/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.nlp.build.aws

import ai.tock.nlp.build.ondemand.WorkerOnDemand
import ai.tock.nlp.build.ondemand.WorkerOnDemandProvider
import ai.tock.nlp.build.ondemand.WorkerOnDemandType
import ai.tock.nlp.build.ondemand.WorkerProperties
import ai.tock.shared.defaultLocale
import ai.tock.shared.intProperty
import ai.tock.shared.property
import java.util.Locale

/**
 * [WorkerOnDemandProvider] aws implementation.
 */
object WorkerOnAwsBatchProvider : WorkerOnDemandProvider {
    override val workerOnDemandType: WorkerOnDemandType
        get() = "AWS_BATCH"

    override fun provide(workerProperties: WorkerProperties): WorkerOnDemand = WorkerOnAwsBatch(
        workerOnAwsBatchProperties = WorkerOnAwsBatchProperties(
            jobDefinitionName = property("tock_worker_aws_batch_job_definition_name", "tock-worker-job-definition"),
            jobQueueName = property("tock_worker_aws_batch_job_queue_name", "tock-worker-job-queue"),
            jobName = property("tock_worker_aws_batch_job_name", "tock-worker-job")
                    + "-${workerProperties["TOCK_BUILD_TYPE"]?.lowercase(defaultLocale)}",
            attemptDurationSeconds = intProperty("tock_worker_aws_batch_attempt_duration_seconds", 7200),
            vcpus = intProperty("tock_worker_aws_batch_vcpus", 4),
            memory = intProperty("tock_worker_aws_batch_memory", 12288)
        ),
        workerProperties = workerProperties
    )
}
