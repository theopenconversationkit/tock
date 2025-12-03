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

package ai.tock.shared

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.jul.LevelChangePropagator
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.classic.spi.Configurator.ExecutionStatus
import ch.qos.logback.classic.spi.Configurator.ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY
import ch.qos.logback.classic.spi.Configurator.ExecutionStatus.INVOKE_NEXT_IF_ANY
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.util.FileSize

/**
 * Default logback configurator.
 */
internal class LogbackConfigurator : ContextAwareBase(), Configurator {
    private val defaultLevel =
        Level.toLevel(
            property(
                "tock_default_log_level",
                if (devEnvironment) "DEBUG" else "INFO",
            ),
        )

    override fun configure(loggerContext: LoggerContext): ExecutionStatus {
        if (booleanProperty("tock_logback_enabled", true)) {
            val c = context

            loggerContext.addListener(
                LevelChangePropagator().apply {
                    context = c
                    start()
                },
            )

            val appender =
                if (booleanProperty("tock_logback_file_appender", false)) {
                    RollingFileAppender<ILoggingEvent>().also {
                        it.name = "file"
                        it.file = "log/logFile.log"
                        it.context = c
                        it.rollingPolicy =
                            TimeBasedRollingPolicy<ILoggingEvent>().apply {
                                fileNamePattern = "log/logFile.%d{yyyy-MM-dd}.log"
                                maxHistory = 30
                                context = c
                                setTotalSizeCap(FileSize.valueOf("3GB"))
                                setParent(it)
                                start()
                            }
                    }
                } else {
                    ConsoleAppender<ILoggingEvent>().apply {
                        name = "console"
                    }
                }

            appender.context = c
            appender.apply {
                encoder =
                    PatternLayoutEncoder().apply {
                        pattern = "%d{yyyy-MM-dd'T'HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n"
                        context = c
                        start()
                    }
            }
            appender.start()

            if (defaultLevel.toInt() <= Level.INFO.toInt()) {
                loggerContext.getLogger("org.mongodb.driver").apply {
                    level = Level.INFO
                    isAdditive = false
                    addAppender(appender)
                }

                loggerContext.getLogger("io.netty").apply {
                    level = Level.INFO
                    isAdditive = false
                    addAppender(appender)
                }

                loggerContext.getLogger("okhttp3").apply {
                    level = Level.INFO
                    isAdditive = false
                    addAppender(appender)
                }

                loggerContext.getLogger("io.mockk").apply {
                    level = Level.INFO
                    isAdditive = false
                    addAppender(appender)
                }
            }

            loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).apply {
                level = defaultLevel
                addAppender(appender)
            }

            return DO_NOT_INVOKE_NEXT_IF_ANY
        } else {
            return INVOKE_NEXT_IF_ANY
        }
    }
}
