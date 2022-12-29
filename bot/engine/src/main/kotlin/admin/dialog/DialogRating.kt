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

package ai.tock.bot.admin.dialog

/**
 * A dialog is a conversation between users and bots.
 * Conversation history is split into a list of [stories].
 * The dialog has a (current) [state].
 */
data class DialogRating(
    /**
     * The state of the dialog.
     */
    /**
     * The history of stories in the dialog.
     */
    /**
     * An optional group identifier.
     */

    var rating: Int? = null,

    var nbUsers: Int? = null
) {

    companion object {
        /**
         * Init a new dialog from the specified dialog.
         */
        fun initFromDialog(dialog: DialogRating): DialogRating {
            return DialogRating(
                rating = dialog.rating,
                nbUsers = dialog.nbUsers
            )
        }
    }


}