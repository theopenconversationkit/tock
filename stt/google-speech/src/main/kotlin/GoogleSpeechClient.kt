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

package ai.tock.stt.google

import ai.tock.shared.error
import ai.tock.stt.STT
import com.google.cloud.speech.v1.RecognitionAudio
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.SpeechClient
import com.google.protobuf.ByteString
import mu.KotlinLogging
import ws.schild.jave.Encoder
import ws.schild.jave.MultimediaObject
import java.io.File
import java.nio.file.Files
import java.util.Locale
import ws.schild.jave.encode.AudioAttributes
import ws.schild.jave.encode.EncodingAttributes

/**
 *
 */
internal object GoogleSpeechClient : STT {

    private val logger = KotlinLogging.logger {}

    private fun parseUnknown(sourceBytes: ByteArray): ByteArray {
        val encoder = Encoder()
        val a = EncodingAttributes()
        val audioA = AudioAttributes()
        audioA.setChannels(1)
        a.setAudioAttributes(audioA)
        a.setInputFormat("flac")
        val sourceFile = File.createTempFile("tock-", ".unknown")
        val targetFile = File.createTempFile("tock-", ".flac")
        return try {
            Files.write(sourceFile.toPath(), sourceBytes)
            encoder.encode(
                MultimediaObject(sourceFile),
                targetFile,
                a
            )
            Files.readAllBytes(targetFile.toPath())
        } finally {
            sourceFile.delete()
            targetFile.delete()
        }
    }

    override fun parse(bytes: ByteArray, language: Locale): String? =
        try {
            SpeechClient.create().use { speechClient ->

                val config = RecognitionConfig.newBuilder()
                    .setEncoding(
                        RecognitionConfig.AudioEncoding.FLAC
                    )
                    .setLanguageCode(language.toString())
                    .build()
                val audio = RecognitionAudio.newBuilder()
                    .setContent(
                        ByteString.copyFrom(parseUnknown(bytes))
                    )
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
