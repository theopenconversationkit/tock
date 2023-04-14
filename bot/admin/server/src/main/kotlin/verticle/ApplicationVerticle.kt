/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.admin.verticle

import ai.tock.bot.admin.test.TestPlanService
import ai.tock.nlp.admin.model.ApplicationScopedQuery
import ai.tock.shared.exception.admin.AdminException
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.toRequestHandler


class ApplicationVerticle : ChildVerticle<AdminException>{

    override fun configure(parent: WebVerticle<AdminException>) {
        with(parent) {

            blockingJsonGet("/application/:applicationId/plans", TockUserRole.botUser,
                handler = toRequestHandler { context ->
                    val applicationId = context.path("applicationId")
                    TestPlanService.getTestPlansByApplication(applicationId).filter { it.namespace == context.organization }
                })

            blockingJsonPost("/application/plans", TockUserRole.botUser,
                handler = toRequestHandler { context, query: ApplicationScopedQuery ->
                    if (context.organization == query.namespace) {
                        TestPlanService.getTestPlansByNamespaceAndNlpModel(query.namespace, query.applicationName)
                    } else {
                        WebVerticle.unauthorized()
                    }
                })

        }
    }

}