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
 *
 * [RatingReportQueryResult] this class contains the statistics concerning the satisfaction module
 *
 * an overall rating is given to the bot
 * the number of users who rated the bot
 * a list of the ratings given with the number of users who gave this rating
 *
 */
data class RatingReportQueryResult(

    /**
     * [ratingBot] the average round bot rating
     * the average rating must be between 1 and 5
     * it can be null if no user has rated the bot or satisfaction is not activated
     */
    val ratingBot: Double?,

    /**
     * [nbUsersRated] the number of users who rated the bot
     */
    val nbUsersRated: Int?,

    /**
     * [ratingDetails] a list of the ratings given with the number of users who gave this rating
     */
    val ratingDetails: List<DialogRating> = emptyList()
)
