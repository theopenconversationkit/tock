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
import ai.tock.nlp.build.ondemand.WorkerOnDemandStatus
import ai.tock.nlp.build.ondemand.WorkerOnDemandSummary
import ai.tock.nlp.build.ondemand.WorkerProperties
import ai.tock.shared.vertx.vertx
import com.amazonaws.services.batch.AWSBatch
import com.amazonaws.services.batch.AWSBatchClientBuilder
import com.amazonaws.services.batch.model.ContainerOverrides
import com.amazonaws.services.batch.model.DescribeJobsRequest
import com.amazonaws.services.batch.model.JobDetail
import com.amazonaws.services.batch.model.JobStatus
import com.amazonaws.services.batch.model.JobSummary
import com.amazonaws.services.batch.model.JobTimeout
import com.amazonaws.services.batch.model.KeyValuePair
import com.amazonaws.services.batch.model.ListJobsRequest
import com.amazonaws.services.batch.model.SubmitJobRequest
import mu.KotlinLogging
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.util.TimeZone

internal class WorkerOnAwsBatch(
    private val workerOnAwsBatchProperties: WorkerOnAwsBatchProperties,
    private val workerProperties: WorkerProperties,
    private val batchClient: AWSBatch = AWSBatchClientBuilder.defaultClient()
) : WorkerOnDemand {

    private val logger = KotlinLogging.logger {}

    private var workerOnDemandSummary = WorkerOnDemandSummary(
        name = workerOnAwsBatchProperties.jobName,
        status = WorkerOnDemandStatus.WAITING
    )

    private val submitJobRequest: SubmitJobRequest
        get() = SubmitJobRequest()
            .withJobName(workerOnAwsBatchProperties.jobName)
            .withJobDefinition(workerOnAwsBatchProperties.jobDefinitionName)
            .withJobQueue(workerOnAwsBatchProperties.jobQueueName)
            .withTimeout(JobTimeout().withAttemptDurationSeconds(workerOnAwsBatchProperties.attemptDurationSeconds))
            .withContainerOverrides(
                ContainerOverrides()
                    .withEnvironment(
                        *workerProperties.toKeyValuePairs().toTypedArray()
                    )
                    .withVcpus(workerOnAwsBatchProperties.vcpus)
                    .withMemory(workerOnAwsBatchProperties.memory)
            )

    private fun summary(jobId: String): WorkerOnDemandSummary = batchClient
        .describeJobs(
            DescribeJobsRequest().withJobs(jobId)
        )
        .run {
            jobs
                .first { it.jobId == jobId }
                .apply {
                    logger.info { "Job $jobName with id $jobId is $status" }
                }
                .toWorkerOnDemandSummary()
        }

    private fun runningJob(): JobSummary? =
        jobSummaryByStatus(JobStatus.RUNNING)
            ?: jobSummaryByStatus(JobStatus.PENDING)
            ?: jobSummaryByStatus(JobStatus.RUNNABLE)
            ?: jobSummaryByStatus(JobStatus.SUBMITTED)
            ?: jobSummaryByStatus(JobStatus.STARTING)

    private fun jobSummaryByStatus(jobStatus: JobStatus): JobSummary? {
        return batchClient
            .listJobs(
                ListJobsRequest()
                    .withJobQueue(workerOnAwsBatchProperties.jobQueueName)
                    .withJobStatus(jobStatus)
            )
            .run {
                this.jobSummaryList.firstOrNull { it.jobName == workerOnAwsBatchProperties.jobName }
            }
    }

    override fun start(callback: (status: WorkerOnDemandStatus) -> Unit) {
        logger.info("WorkerOnAwsBatch starting for ${workerProperties["TOCK_BUILD_TYPE"]} build type")

        val jobId = runningJob()?.jobId
            ?: batchClient.submitJob(submitJobRequest).jobId

        vertx.setPeriodic(Duration.ofSeconds(10).toMillis()) { periodicId ->

            with(summary(jobId)) {
                when (status) {
                    WorkerOnDemandStatus.SUCCEEDED -> {
                        logger.info("Job $jobId succeeded")
                        callback(WorkerOnDemandStatus.SUCCEEDED)
                        vertx.cancelTimer(periodicId)
                    }
                    WorkerOnDemandStatus.FAILED -> {
                        logger.info("Job $jobId failed")
                        callback(WorkerOnDemandStatus.FAILED)
                        vertx.cancelTimer(periodicId)
                    }
                    WorkerOnDemandStatus.RUNNING -> {
                        logger.info("Job $jobId is running")
                    }
                    WorkerOnDemandStatus.WAITING -> {
                        logger.info("Job $jobId is waiting")
                    }
                }
            }
        }
    }

    override fun summary(): WorkerOnDemandSummary = workerOnDemandSummary

    private fun WorkerProperties.toKeyValuePairs() = map {
        KeyValuePair().withName(it.key).withValue(it.value)
    }

    private fun workerOnDemandSummary(jobName: String, jobId: String, createdAt: Long, status: String) = WorkerOnDemandSummary(
        name = jobName,
        id = jobId,
        startingDate = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(createdAt),
            TimeZone.getDefault().toZoneId()
        ),
        status = when (JobStatus.fromValue(status)) {
            JobStatus.SUBMITTED -> WorkerOnDemandStatus.RUNNING
            JobStatus.PENDING -> WorkerOnDemandStatus.RUNNING
            JobStatus.RUNNABLE -> WorkerOnDemandStatus.RUNNING
            JobStatus.STARTING -> WorkerOnDemandStatus.RUNNING
            JobStatus.RUNNING -> WorkerOnDemandStatus.RUNNING
            JobStatus.SUCCEEDED -> WorkerOnDemandStatus.SUCCEEDED
            else -> WorkerOnDemandStatus.FAILED
        }
    )

    private fun JobDetail.toWorkerOnDemandSummary() =
        workerOnDemandSummary(jobName, jobId, createdAt, status)
}
