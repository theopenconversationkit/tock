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

package ai.tock.bot.admin

import ai.tock.bot.admin.annotation.BotAnnotationEventComment
import ai.tock.bot.admin.dialog.ActionReport
import ai.tock.bot.admin.dialog.DialogReport

/**
 * [BotAnnotationEventComment.canEdit] is not persisted (it defaults to `false`): it is derived at
 * read time as "the requesting user authored the comment". Every read path that returns annotations
 * to a user must apply this, otherwise the author loses the edit/delete buttons on their own
 * comments after a reload.
 */
fun ActionReport.withCommentRights(userLogin: String): ActionReport =
    copy(
        annotation =
            annotation?.let { annotation ->
                annotation.copy(
                    events =
                        annotation.events
                            .map { event ->
                                if (event is BotAnnotationEventComment) {
                                    event.copy(canEdit = event.user == userLogin)
                                } else {
                                    event
                                }
                            }.toMutableList(),
                )
            },
    )

/** Applies [withCommentRights] to every action of the dialog. */
fun DialogReport.withCommentRights(userLogin: String): DialogReport = copy(actions = actions.map { it.withCommentRights(userLogin) })
