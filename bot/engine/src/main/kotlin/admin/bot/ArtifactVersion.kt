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

import kotlin.math.abs

/**
 * An artifact version number.
 */
data class ArtifactVersion(
    val major: String,
    val minor: String,
    val iteration: String
) {

    companion object {
        /**
         * The "unknown" artifact version number.
         */
        val UNKNOWN: ArtifactVersion = ArtifactVersion("NONE", "NONE", "NONE")

        private fun distance(v1: String, v2: String): Long {
            return if (v1 == v2) {
                0
            } else if (v1.toLongOrNull() != null && v2.toLongOrNull() != null) {
                abs(v1.toLong() - v2.toLong())
            } else {
                levenshtein(v1, v2).toLong()
            }
        }

        private fun levenshtein(
            s: String,
            t: String,
            charScore: (Char, Char) -> Int = { c1, c2 -> if (c1 == c2) 0 else 1 }
        ): Int {

            // Special cases
            if (s == t) return 0
            if (s == "") return t.length
            if (t == "") return s.length

            val initialRow: List<Int> = (0 until t.length + 1).map { it }.toList()
            return (0 until s.length).fold(
                initialRow,
                { previous, u ->
                    (0 until t.length).fold(
                        mutableListOf(u + 1),
                        { row, v ->
                            row.add(
                                listOf(
                                    row.last() + 1,
                                    previous[v + 1] + 1,
                                    previous[v] + charScore(s[u], t[v])
                                ).minOrNull()!!
                            )
                            row
                        }
                    )
                }
            ).last()
        }
    }

    internal fun distanceFrom(version: ArtifactVersion): Long {
        return distance(major, version.major) * 100 + distance(minor, version.minor) * 10 + distance(iteration, version.iteration)
    }
}
