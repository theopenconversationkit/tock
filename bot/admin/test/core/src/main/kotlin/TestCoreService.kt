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

package ai.tock.bot.admin.test

import ai.tock.bot.admin.test.model.BotDialogRequest
import ai.tock.bot.admin.test.model.TestPlanUpdate
import ai.tock.nlp.admin.AdminVerticle
import ai.tock.nlp.admin.model.ApplicationScopedQuery
import ai.tock.nlp.front.client.FrontClient
import ai.tock.shared.Dice
import ai.tock.shared.security.TockUserRole.botUser
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.WebVerticle.Companion
import io.vertx.ext.web.RoutingContext
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Duration
import java.time.Instant

class TestCoreService : TestService {
    override fun registerServices(): (AdminVerticle).() -> Unit =
        {
            fun RoutingContext.loadTestPlan(): TestPlan =
                TestPlanService.getTestPlan(pathId("planId"))?.run {
                    if (organization != namespace) {
                        Companion.unauthorized()
                    } else {
                        this
                    }
                } ?: Companion.notFound()

            blockingJsonGet("/test/plans", botUser) { context ->
                TestPlanService.getTestPlansByNamespace(context.organization)
            }

            blockingJsonGet("/test/plan/:planId", botUser) { context ->
                context.loadTestPlan()
            }

            blockingJsonGet("/test/plan/:planId/executions", botUser) { context ->
                TestPlanService.getPlanExecutions(context.loadTestPlan())
            }

            blockingJsonGet("/test/plan/:planId/executions/:executionId", botUser) { context ->
                TestPlanService.getTestPlanExecution(context.loadTestPlan(), context.pathId("executionId"))
            }

            blockingJsonPost(
                "/test/plan",
                botUser,
                logger<TestPlanUpdate>("Update Test Plan") { _, p ->
                    p?.let { FrontClient.getApplicationByNamespaceAndName(it.namespace, it.nlpModel)?._id }
                },
            ) { context, plan: TestPlanUpdate ->
                if (context.organization == plan.namespace) {
                    TestPlanService.saveTestPlan(plan.toTestPlan())
                } else {
                    WebVerticle.unauthorized()
                }
            }

            blockingDelete(
                "/test/plan/:planId",
                botUser,
                simpleLogger("Delete Test Plan", { it.path("planId") to true }),
            ) { context ->
                TestPlanService.removeTestPlan(context.loadTestPlan())
            }

            blockingJsonPost(
                "/test/plan/:planId/dialog/:dialogId",
                botUser,
                simpleLogger("Add Dialog to Test Plan", { it.path("planId") to it.path("dialogId") }),
            ) { context, _: ApplicationScopedQuery ->
                TestPlanService.addDialogToTestPlan(context.loadTestPlan(), context.pathId("dialogId"))
            }

            blockingJsonPost(
                "/test/plan/:planId/dialog/delete/:dialogId",
                botUser,
                simpleLogger("Remove Dialog from Test Plan", { it.path("planId") to it.path("dialogId") }),
            ) { context, _: ApplicationScopedQuery ->
                TestPlanService.removeDialogFromTestPlan(
                    context.loadTestPlan(),
                    context.pathId("dialogId"),
                )
            }

            blockingJsonPost("/test/plan/execute", botUser) { context, testPlan: TestPlan ->
                saveAndExecuteTestPlan(context.organization, testPlan, newId())
            }

            /**
             * Triggered on click on "Launch" button.
             */
            blockingJsonPost("/test/plan/:planId/run", botUser) { context, _: ApplicationScopedQuery ->
                context.loadTestPlan().run {
                    executeTestPlan(this)
                }
            }

            blockingJsonPost("/test/talk", setOf(botUser)) { context, query: BotDialogRequest ->
                if (context.organization == query.namespace) {
                    val debugEnabled = context.queryParams()["debug"]?.toBoolean() ?: false
                    val sourceWithContent = context.queryParams()["sourceWithContent"]?.toBoolean() ?: false
                    TestTalkService.talk(query, debugEnabled, sourceWithContent)
                } else {
                    Companion.unauthorized()
                }
            }
        }

    /**
     * This function saves the current test plan in the mongo database and
     * executes all test contained in the common test plan.
     *
     */
    override fun saveAndExecuteTestPlan(
        namespace: String,
        testPlan: TestPlan,
        executionId: Id<TestPlanExecution>,
    ): TestPlanExecution =
        TestTalkService
            .getBotConfiguration(testPlan.botApplicationConfigurationId, namespace)
            .let {
                TestPlanService.saveAndRunTestPlan(
                    TestTalkService.getRestClient(it),
                    testPlan,
                    executionId,
                )
            }

    private fun executeTestPlan(testPlan: TestPlan): Id<TestPlanExecution> {
        val executionId = Dice.newId()
        val exec =
            TestPlanExecution(
                testPlanId = testPlan._id,
                dialogs = mutableListOf(),
                nbErrors = 0,
                duration = Duration.between(Instant.now(), Instant.now()),
                _id = executionId.toId(),
                status = TestPlanExecutionStatus.PENDING,
            )
        // save the test plan execution into the database
        TestPlanService.saveTestPlanExecution(exec)
        TestPlanService.runTestPlan(
            TestTalkService.getRestClient(
                TestTalkService.getBotConfiguration(
                    testPlan.botApplicationConfigurationId,
                    testPlan.namespace,
                ),
            ),
            testPlan,
            executionId.toId(),
        )
        return executionId.toId()
    }

    override fun priority(): Int = 0
}
