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

package ai.tock.shared.vertx

import ai.tock.shared.Executor
import ai.tock.shared.ImageFormat
import ai.tock.shared.ImageGenerator
import ai.tock.shared.ImageParametersExtractor
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.provide
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging

/**
 * Provides a service that generates image using [imageGenerator] specified by [paramExtractor].
 */
class ImageGeneratorHandler<T : Any>(
    private val imageGenerator: ImageGenerator<T>,
    private val paramExtractor: ImageParametersExtractor<T>,
    private val executor: Executor = injector.provide()
) : Handler<RoutingContext> {

    private val logger = KotlinLogging.logger {}
    override fun handle(context: RoutingContext) {
        executor.executeBlocking {
            try {
                val requestParams = context.request().params()
                val imageParams = paramExtractor.extract(requestParams)
                if (imageParams == null) {
                    context.response().setStatusCode(404).end()
                } else {
                    val format = requestParams["format"]?.let {
                        ImageFormat.findByCode(
                            it
                        )
                    } ?: ImageFormat.PNG
                    val data = imageGenerator.generate(imageParams, format)
                    context.response().putHeader(HttpHeaders.CONTENT_LENGTH, data.size.toString())
                    context.response().putHeader(HttpHeaders.CONTENT_TYPE, format.contentType)
                    context.response().write(Buffer.buffer(data))
                    context.response().end()
                }
            } catch (e: Throwable) {
                logger.error(e)
                context.fail(e)
            }
        }
    }
}
