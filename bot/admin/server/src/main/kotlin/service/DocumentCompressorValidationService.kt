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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.bot.compressor.BotDocumentCompressorConfiguration
import ai.tock.genai.orchestratorclient.requests.DocumentCompressorProviderSettingStatusQuery
import ai.tock.genai.orchestratorclient.responses.ProviderSettingStatusResponse
import ai.tock.genai.orchestratorclient.services.DocumentCompressorProviderService
import ai.tock.shared.exception.error.ErrorMessage
import ai.tock.shared.injector
import ai.tock.shared.provide


object DocumentCompressorValidationService {

    private val documentCompressorProviderService: DocumentCompressorProviderService get() = injector.provide()

    fun validate(config: BotDocumentCompressorConfiguration): Set<ErrorMessage> {
        return mutableSetOf<ErrorMessage>().apply {
            addAll(
                documentCompressorProviderService
                    .checkSetting(
                        DocumentCompressorProviderSettingStatusQuery(
                            setting = config.setting
                        )
                    )
                    .getErrors("Document Compressor setting check failed")
            )
        }
    }

    private fun ProviderSettingStatusResponse?.getErrors(message: String): Set<ErrorMessage> =
        this?.errors?.map { ErrorMessage(message = message, params = errors) }?.toSet() ?: emptySet()

}