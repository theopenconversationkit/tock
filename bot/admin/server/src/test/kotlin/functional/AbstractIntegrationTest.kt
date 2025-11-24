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
@file:Suppress("ktlint:standard:property-naming")

package ai.tock.bot.admin.functional

import ai.tock.bot.BotIoc
import ai.tock.bot.admin.BotAdminVerticle
import ai.tock.nlp.front.ioc.FrontIoc
import ai.tock.shared.injector
import ai.tock.shared.vertx.vertx
import com.github.salomonbrys.kodein.Kodein
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.extension.ExtendWith
import org.litote.kmongo.id.ObjectIdToStringGenerator
import org.litote.kmongo.id.StringId
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@ExtendWith(VertxExtension::class)
@Disabled
open class AbstractIntegrationTest {
    companion object {
        @JvmStatic
        protected lateinit var authCookie: String

        @JvmStatic
        protected lateinit var webClient: WebClient

        const val userLogin = "admin@app.com"

        lateinit var kodein: Kodein

        @JvmStatic
        @BeforeAll
        fun setup(context: VertxTestContext) {
            // Use checkpoints to make test waits for completion of multiple conditions
            val verticleDeployed = context.checkpoint()
            val authCookieReceived = context.checkpoint()
            // Up the mongo test db docker container
            container.start()
            val conString = container.connectionString
            // Setup connection string to the tock system property
            System.setProperty("tock_mongo_url", conString)
            // Perform DI
            FrontIoc.setup(BotIoc.coreModules)
            // deploy verticle
            vertx.deployVerticle(BotAdminVerticle()).onComplete {
                verticleDeployed.flag()
            }
            kodein = injector.kodein().value
            // rest client for integral tests
            webClient =
                WebClient.create(
                    vertx,
                    WebClientOptions()
                        .setDefaultPort(8080)
                        .setDefaultHost("localhost"),
                )
            // Receive authentication header
            webClient.post("/rest/authenticate")
                .sendBuffer(
                    JsonObject().put("email", userLogin).put("password", "password").toBuffer(),
                ).onComplete {
                    authCookie = it.result().cookies().first()
                    authCookieReceived.flag()
                }
        }

        val container: MongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:4.4.24"))

        init {
            System.setProperty("tock_bot_encrypted_flags", "test1,test2")
            System.setProperty("tock_encrypt_pass", "dev")
            System.setProperty("tock_front_mongo_db", "test_tock_front_db")
            System.setProperty("tock_bot_mongo_db", "test_tock_bot_db")
            System.setProperty("tock_model_mongo_db", "test_tock_model_mongo_db")
            System.setProperty("tock_cache_mongo_db", "test_tock_cache_mongo_db")
        }

        @AfterAll
        @JvmStatic
        fun erase() {
            container.close()
        }

        /**
         * Generates the same type of IDs for MongoDB like the Backend apps do
         */
        fun <T> newId(): StringId<T> {
            return ObjectIdToStringGenerator.generateNewId()
        }
    }
}
