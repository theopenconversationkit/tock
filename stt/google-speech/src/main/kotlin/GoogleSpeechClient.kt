/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package ai.tock.stt.google

import com.google.cloud.speech.v1.RecognitionAudio
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.SpeechClient
import com.google.protobuf.ByteString
import ai.tock.shared.error
import ai.tock.stt.AudioCodec
import ai.tock.stt.AudioCodec.unknown
import ai.tock.stt.AudioCodec.ogg
import ai.tock.stt.STT
import mu.KotlinLogging
import ws.schild.jave.AudioAttributes
import ws.schild.jave.Encoder
import ws.schild.jave.EncodingAttributes
import ws.schild.jave.MultimediaObject
import java.io.File
import java.nio.file.Files
import java.util.Locale


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
        a.audioAttributes = audioA
        a.format = "flac"
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

    override fun parse(bytes: ByteArray, language: Locale, codec: AudioCodec): String? =
        try {
            SpeechClient.create().use { speechClient ->

                val config = RecognitionConfig.newBuilder()
                    .setEncoding(
                        when (codec) {
                            ogg -> RecognitionConfig.AudioEncoding.OGG_OPUS
                            unknown -> RecognitionConfig.AudioEncoding.FLAC
                        }
                    )
                    .setLanguageCode(language.toString())
                    .apply {
                        if(codec == ogg) {
                            sampleRateHertz = 16000
                        }
                    }
                    .build()
                val audio = RecognitionAudio.newBuilder()
                    .setContent(
                        ByteString.copyFrom(
                            when (codec) {
                                ogg -> bytes
                                unknown -> parseUnknown(bytes)
                            }
                        )
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