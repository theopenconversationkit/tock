package ai.tock.nlp.dialogflow

import com.google.cloud.dialogflow.v2.Agent
import com.google.cloud.dialogflow.v2.AgentsClient
import com.google.cloud.dialogflow.v2.Intent
import com.google.cloud.dialogflow.v2.IntentView
import com.google.cloud.dialogflow.v2.IntentsClient
import com.google.cloud.dialogflow.v2.ListIntentsRequest
import com.google.cloud.dialogflow.v2.ProjectAgentName
import com.google.cloud.dialogflow.v2.ProjectName
import com.google.cloud.dialogflow.v2.QueryInput
import com.google.cloud.dialogflow.v2.QueryResult
import com.google.cloud.dialogflow.v2.SessionName
import com.google.cloud.dialogflow.v2.SessionsClient
import com.google.cloud.dialogflow.v2.TextInput
import mu.KotlinLogging


internal object DialogflowService {

    private const val DIALOGFLOW_MAX_TEXT_LENGTH = 256

    private val logger = KotlinLogging.logger {}

    /**
     * Returns the result of detect intent with text as input.
     *
     * Using the same `session_id` between requests allows continuation of the conversation.
     *
     * @param projectId    Project/Agent Id.
     * @param text         The text intent to be detected based on what a user says.
     * @param sessionId    Identifier of the DetectIntent session.
     * @param languageCode Language code of the query.
     * @return The QueryResult for the input text.
     */
    fun detectIntentText(
        projectId: String,
        text: String,
        sessionId: String,
        languageCode: String
    ): QueryResult? {

        SessionsClient.create().use {
            // Set the session name using the sessionId (UUID) and projectID (my-project-id)
            val session = SessionName.of(projectId, sessionId)
            logger.debug("Session Path: $session")

            val dialogflowText = if (text.length > DIALOGFLOW_MAX_TEXT_LENGTH) {
                text.substring(0, DIALOGFLOW_MAX_TEXT_LENGTH).also {
                    logger.warn { "Text sent to Dialogflow too long : More than ${text.length} characters. Truncated to $DIALOGFLOW_MAX_TEXT_LENGTH characters." }
                }
            } else {
                text
            }

            // Set the text (hello) and language code (en-US) for the query
            TextInput.newBuilder().setText(dialogflowText).setLanguageCode(languageCode)
                .apply {
                    QueryInput.newBuilder().setText(this).build().apply {
                        it.detectIntent(session, this).apply {
                            return this.queryResult.also {
                                logger.debug{"Query Text: '${it.queryText}'"}
                                logger.debug{"Detected Intent: ${it.intent.displayName} (confidence: ${it.intentDetectionConfidence})"}
                            }
                        }
                    }
                }
        }
        return null
    }

    /**
     * Retrieves the [Agent] of the given [projectId].
     */
    fun getAgent(projectId: String): Agent? {
        AgentsClient.create().use {
            val parent: ProjectName = ProjectName.of(projectId)
            return it.getAgent(parent)
        }
    }

    /**
     * Get intents with training phrases
     */
    fun getIntents(projectId: String): List<Intent> {
        IntentsClient.create().use {
            val parent = ProjectAgentName.of(projectId)
            val request  = ListIntentsRequest.newBuilder().setIntentView(IntentView.INTENT_VIEW_FULL).setParent(parent.toString()).build()
            return it.listIntents(request).iterateAll().asSequence().toList()
        }
    }

}

