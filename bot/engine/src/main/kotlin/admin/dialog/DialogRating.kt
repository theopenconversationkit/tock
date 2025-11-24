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

package ai.tock.bot.admin.dialog

/**
 * A [DialogRating] is a class of statistics for the user satisfaction module
 * for each rating given by a user, the number of users who gave the same rating
 *
 */
data class DialogRating(
    /**
     *  [rating] is the rating given by the user
     *  the rating can be a number between 1 and 5
     */
    var rating: Double? = null,
    /**
     * [nbUsers] is the number of users who gave this rating to the dialog
     */
    var nbUsers: Int? = null,
)
