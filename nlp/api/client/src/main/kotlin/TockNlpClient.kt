/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.nlp.api.client

import ai.tock.nlp.api.client.model.NlpLogCount
import ai.tock.nlp.api.client.model.NlpLogCountQuery
import ai.tock.nlp.api.client.model.NlpQuery
import ai.tock.nlp.api.client.model.NlpResult
import ai.tock.nlp.api.client.model.dump.ApplicationDefinition
import ai.tock.nlp.api.client.model.dump.ApplicationDump
import ai.tock.nlp.api.client.model.dump.CreateApplicationQuery
import ai.tock.nlp.api.client.model.dump.IntentDefinition
import ai.tock.nlp.api.client.model.dump.SentencesDump
import ai.tock.nlp.api.client.model.evaluation.EntityEvaluationQuery
import ai.tock.nlp.api.client.model.evaluation.EntityEvaluationResult
import ai.tock.nlp.api.client.model.merge.ValuesMergeQuery
import ai.tock.nlp.api.client.model.merge.ValuesMergeResult
import ai.tock.nlp.api.client.model.monitoring.MarkAsUnknownQuery
import ai.tock.shared.retrofitDefaultLogLevel
import ai.tock.shared.retrofitLogLevel
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 *  Wraps calls to the NLP stack.
 */
class TockNlpClient(baseUrl: String = System.getenv("tock_nlp_service_url") ?: "http://localhost:8888") : NlpClient {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val nlpService: NlpService

    init {
        val mapper = jacksonObjectMapper()
        mapper.findAndRegisterModules()
        // force java time module
        mapper.registerModule(JavaTimeModule())
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)

        val timeout = longProperty("tock_nlp_client_request_timeout_ms", 20000)
        val retrofit = Retrofit.Builder()
            .baseUrl("$baseUrl/rest/nlp/")
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .client(
                OkHttpClient.Builder()
                    .readTimeout(timeout, TimeUnit.MILLISECONDS)
                    .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                    .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                    .addInterceptor(
                        // adapt log level to use specific log interceptors
                        HttpLoggingInterceptor().setLevel(
                            HttpLoggingInterceptor.Level.valueOf(
                                retrofitLogLevel(
                                    retrofitDefaultLogLevel
                                ).toString()
                            )
                        )
                    )
                    .build()
            )
            .build()
        nlpService = retrofit.create(NlpService::class.java)
    }

    private fun longProperty(name: String, defaultValue: Long): Long = System.getenv(name)?.toLong() ?: defaultValue

    private fun <T> Response<T>.parseAndReturns(): T? =
        body()
            ?: run {
                logger.error { "nlp error : ${errorBody()?.string()}" }
                null
            }

    override fun parse(query: NlpQuery): NlpResult? {
        return nlpService.parse(query).execute().parseAndReturns()
    }

    override fun evaluateEntities(query: EntityEvaluationQuery): EntityEvaluationResult? {
        return nlpService.evaluateEntities(query).execute().parseAndReturns()
    }

    override fun mergeValues(query: ValuesMergeQuery): ValuesMergeResult? {
        return nlpService.mergeValues(query).execute().parseAndReturns()
    }

    override fun markAsUnknown(query: MarkAsUnknownQuery) {
        nlpService.markAsUnknown(query).execute()
    }

    override fun createApplication(namespace: String, name: String, locale: Locale): ApplicationDefinition? {
        return nlpService.createApplication(CreateApplicationQuery(name, namespace = namespace, locale = locale))
            .execute().body()
    }

    private fun createMultipart(stream: InputStream): MultipartBody.Part {
        val dump = ByteArrayOutputStream().apply {
            var nRead: Int = 0
            val data = ByteArray(2048)
            while (nRead != -1) {
                nRead = stream.read(data, 0, data.size)
                if (nRead != -1)
                    write(data, 0, Math.min(nRead, data.size))
            }
            flush()
        }
        return MultipartBody.Part.createFormData(
            "dump",
            "dump",
            RequestBody.create(MultipartBody.FORM, dump.toByteArray())
        )
    }

    override fun getIntentsByNamespaceAndName(namespace: String, name: String): List<IntentDefinition>? {
        return nlpService.getIntentsByNamespaceAndName(namespace, name).execute().parseAndReturns()
    }

    override fun getApplicationByNamespaceAndName(namespace: String, name: String): ApplicationDefinition? {
        return nlpService.getApplicationByNamespaceAndName(namespace, name).execute().parseAndReturns()
    }

    override fun importNlpDump(stream: InputStream): Boolean {
        return nlpService.importNlpDump(createMultipart(stream)).execute().body()?.success ?: false
    }

    override fun importNlpPlainDump(dump: ApplicationDump): Boolean {
        return nlpService.importNlpPlainDump(dump).execute().body()?.success ?: false
    }

    override fun importNlpSentencesDump(stream: InputStream): Boolean {
        return nlpService.importNlpSentencesDump(createMultipart(stream)).execute().body()?.success ?: false
    }

    override fun importNlpPlainSentencesDump(dump: SentencesDump): Boolean {
        return nlpService.importNlpPlainSentencesDump(dump).execute().body()?.success ?: false
    }

    override fun logsCount(query: NlpLogCountQuery): List<NlpLogCount>? {
        return nlpService.logsCount(query).execute().parseAndReturns()
    }

    override fun healthcheck(): Boolean {
        return try {
            nlpService.healthcheck().execute().isSuccessful
        } catch (t: Throwable) {
            false
        }
    }
}
