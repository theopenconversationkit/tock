/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.admin.test.xray.model

/**
 * see (https://confluence.xpand-addons.com/display/XRAY/Tests+-+REST)
 */
data class JiraTest(val fields: Map<String, Any>) {

    constructor(project: String,
                summary: String,
                description: String,
                testTypeField: String,
                stepField: String)
            : this(
            mapOf(
                    "project" to JiraTestProject(project),
                    "summary" to summary,
                    "description" to description,
                    "issuetype" to JiraIssueType("Test"),
                    testTypeField to mapOf("value" to "Manual"),
                    stepField to mapOf("steps" to emptyList<XrayAttachment>())
            )
    )

}