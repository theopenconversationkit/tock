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

package ai.tock.bot.connector.mattermost

import ai.tock.bot.connector.ConnectorCallbackBase

/**
 * Keeps the Mattermost post that triggered the request, so that the answer can be sent in its thread.
 *
 * Mattermost only accepts the root post of a thread as `root_id`, and the outgoing webhook only gives the id
 * of the triggering post (which may be a reply). [threadRootResolver] looks up the root post; it is called
 * at most once per request, when the first answer is sent.
 */
internal class MattermostConnectorCallback(
    override val applicationId: String,
    val channelId: String,
    val postId: String?,
    private val threadRootResolver: (String) -> String? = { null },
) : ConnectorCallbackBase(applicationId, mattermostConnectorType) {
    /**
     * The root post of the thread of [postId], or [postId] itself if it cannot be resolved.
     */
    val threadRootId: String? by lazy { postId?.let { threadRootResolver(it) ?: it } }
}
