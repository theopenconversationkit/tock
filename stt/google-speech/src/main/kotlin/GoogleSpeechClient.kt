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

package fr.vsct.tock.stt.google

import com.google.cloud.speech.v1.RecognitionAudio
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.SpeechClient
import com.google.protobuf.ByteString
import fr.vsct.tock.shared.error
import fr.vsct.tock.stt.STT
import mu.KotlinLogging
import java.util.Locale

/**
 *
 */
internal object GoogleSpeechClient : STT {

    private val logger = KotlinLogging.logger {}

    override fun parse(bytes: ByteArray, language: Locale): String? =
        try {
            SpeechClient.create().use { speechClient ->

                val config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED)
                    .setLanguageCode(language.toString())
                    .build()
                val audio = RecognitionAudio.newBuilder()
                    .setContent(ByteString.copyFrom(bytes))
                    .build()
                val response = speechClient.recognize(config, audio)
                logger.info { response }
                response.getResults(0).getAlternatives(0).transcript
            }
        } catch (e: Exception) {
            logger.error(e)
            null
        }

}