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

package ai.tock.storage.dynamodb.publication

import ai.tock.nlp.front.service.storage.ApplicationDefinitionDAO
import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import ai.tock.nlp.front.service.storage.IntentDefinitionDAO
import ai.tock.nlp.front.service.storage.NamespaceConfigurationDAO
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.nlp.front.storage.dynamodb.ClassifiedSentenceDynamoDAO
import ai.tock.nlp.front.storage.dynamodb.ClassifiedSentenceDynamoDAO.SentenceKey
import ai.tock.storage.dynamodb.DocumentCodec
import mu.KotlinLogging
import java.security.MessageDigest
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.Base64

/** The NLP DAOs of the admin (MongoDB), read by the publication. */
class NlpSources(
    val applications: ApplicationDefinitionDAO,
    val intents: IntentDefinitionDAO,
    val entityTypes: EntityTypeDefinitionDAO,
    val sentences: ClassifiedSentenceDAO,
    /** The namespace configurations: the intents imported by the applications of a namespace. */
    val namespaces: NamespaceConfigurationDAO? = null,
)

/**
 * Publishes the validated sentences of the admin (status [validated] or [model]) to DynamoDB, for the exact
 * search of the parser.
 *
 * The admin can have tens of thousands of validated sentences: reading them in DynamoDB at each publication
 * would cost more than the rest of the storage. The fingerprints of the published sentences are kept in memory,
 * read from DynamoDB once, when the first publication runs.
 *
 * - an incremental publication reads in MongoDB the sentences modified since the previous publication (the
 *   `updateDate` set by each save), writes the validated ones that changed and deletes the published ones that are
 *   not validated anymore;
 * - a complete publication reads every validated sentence: it also finds the sentences deleted from MongoDB and
 *   the bulk updates of MongoDB that do not change `updateDate` (the intent of the sentences switched or the
 *   entities removed when an intent or an entity is deleted). It runs at the first publication, when
 *   [requestComplete] is called (a change of the applications, intents or entity types), and every
 *   [completeInterval].
 *
 * The runtime does not replace a published sentence (see [ClassifiedSentenceDynamoDAO.save]): the fingerprints in
 * memory stay those of DynamoDB.
 */
internal class SentencePublication(
    private val sources: NlpSources,
    private val completeInterval: Duration,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val logger = KotlinLogging.logger {}

    /** What the parser reads in a sentence: a change of the other fields (counters, probabilities) is not published. */
    private data class PublishedContent(
        val text: String,
        val status: ClassifiedSentenceStatus,
        val classification: Classification,
    )

    data class Result(
        val written: Int,
        val deleted: Int,
    )

    private var published: MutableMap<SentenceKey, String>? = null
    private var lastPublication: Instant? = null
    private var lastComplete: Instant? = null

    @Volatile
    private var completeRequested = false

    /** The next publication is complete. */
    fun requestComplete() {
        completeRequested = true
    }

    /** The modified sentences are read from this delay before the previous publication: the clocks may differ a little. */
    private val margin = Duration.ofMinutes(1)

    fun publish(): Result {
        val start = clock.instant()
        val fingerprints = published ?: ClassifiedSentenceDynamoDAO.publishedFingerprints().toMutableMap().also { published = it }
        val since = lastPublication
        val complete = completeRequested || since == null || lastComplete?.let { Duration.between(it, start) >= completeInterval } != false
        completeRequested = false
        val result = if (complete) publishAll(fingerprints) else publishModified(fingerprints, since.minus(margin))
        lastPublication = start
        if (complete) lastComplete = start
        return result
    }

    private fun ClassifiedSentence.isPublished() = status == validated || status == model

    private fun fingerprint(sentence: ClassifiedSentence): String =
        Base64.getEncoder().encodeToString(
            MessageDigest
                .getInstance("SHA-256")
                .digest(DocumentCodec.mapper.writeValueAsBytes(PublishedContent(sentence.text, sentence.status, sentence.classification))),
        )

    private fun publishAll(fingerprints: MutableMap<SentenceKey, String>): Result {
        val sentences = (sources.sentences.getSentences(null, null, validated) + sources.sentences.getSentences(null, null, model)).associateBy { ClassifiedSentenceDynamoDAO.keyOf(it) }
        val deleted = fingerprints.keys - sentences.keys
        unpublish(fingerprints, deleted)
        val written = write(fingerprints, sentences.values)
        logger.debug { "complete publication of ${sentences.size} validated sentences" }
        return Result(written, deleted.size)
    }

    private fun publishModified(
        fingerprints: MutableMap<SentenceKey, String>,
        since: Instant,
    ): Result {
        val modified =
            sources.applications.getApplications().flatMap { application ->
                sources.sentences
                    .search(
                        SentencesQuery(
                            application._id,
                            size = null,
                            notStatus = null,
                            modifiedAfter = since.atZone(ZoneOffset.UTC),
                        ),
                    ).sentences
            }
        val (validatedSentences, others) = modified.partition { it.isPublished() }
        val deleted = others.map { ClassifiedSentenceDynamoDAO.keyOf(it) }.filter { it in fingerprints } - validatedSentences.map { ClassifiedSentenceDynamoDAO.keyOf(it) }.toSet()
        unpublish(fingerprints, deleted)
        return Result(write(fingerprints, validatedSentences), deleted.size)
    }

    private fun write(
        fingerprints: MutableMap<SentenceKey, String>,
        sentences: Collection<ClassifiedSentence>,
    ): Int {
        val changed =
            sentences
                .map { it to fingerprint(it) }
                .filter { (sentence, fingerprint) -> fingerprints[ClassifiedSentenceDynamoDAO.keyOf(sentence)] != fingerprint }
        // in batches: a failure keeps the fingerprints of the batches already written
        changed.chunked(BATCH).forEach { batch ->
            ClassifiedSentenceDynamoDAO.publish(batch)
            batch.forEach { (sentence, fingerprint) -> fingerprints[ClassifiedSentenceDynamoDAO.keyOf(sentence)] = fingerprint }
        }
        return changed.size
    }

    private fun unpublish(
        fingerprints: MutableMap<SentenceKey, String>,
        keys: Collection<SentenceKey>,
    ) {
        keys.chunked(BATCH).forEach { batch ->
            ClassifiedSentenceDynamoDAO.unpublish(batch)
            batch.forEach { fingerprints.remove(it) }
        }
    }

    private companion object {
        const val BATCH = 500
    }
}
