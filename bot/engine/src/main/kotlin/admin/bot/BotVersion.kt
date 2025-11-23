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

package ai.tock.bot.admin.bot

import ai.tock.bot.admin.bot.ArtifactVersion.Companion.UNKNOWN

/**
 * A bot version number.
 */
data class BotVersion(val botVersion: ArtifactVersion, val tockVersion: ArtifactVersion) {
    companion object {
        /**
         * Get the current bot version.
         */
        fun getCurrentBotVersion(botId: String): BotVersion {
            // TODO
            return BotVersion(UNKNOWN, UNKNOWN)
        }

        internal fun findBestMatchVersion(
            versions: List<BotVersion>,
            targetVersion: BotVersion,
        ): BotVersion? {
            // 1 use the tock version
            return versions.groupBy { it.tockVersion.distanceFrom(targetVersion.tockVersion) }
                .minByOrNull { it.key }
                ?.value
                ?.run {
                    if (size > 1) {
                        // 2 use bot version
                        groupBy { it.botVersion.distanceFrom(targetVersion.botVersion) }
                            .minByOrNull { it.key }
                            ?.value
                            ?.firstOrNull()
                    } else {
                        firstOrNull()
                    }
                }
        }
    }
}
