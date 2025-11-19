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

package builder

import ai.tock.bot.connector.googlechat.GoogleChatConnectorCardMessageOut
import ai.tock.bot.connector.googlechat.builder.ChatIcon
import ai.tock.bot.connector.googlechat.builder.card
import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.I18nTranslator
import ai.tock.translator.TranslatedString
import com.google.api.client.json.jackson2.JacksonFactory
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GoogleChatCardMessageBuildersTest {
    private val i18nTranslator =
        mockk<I18nTranslator> {
            every { translate(text = any()) } answers { TranslatedString(arg(0)) }
        }

    private fun buildCard(): GoogleChatConnectorCardMessageOut =
        i18nTranslator.card {
            header("Pizza Bot Customer Support", "pizzabot@example.com", "https://goo.gl/aeDtrS")
            section {
                keyValue("Order No.", "12345", action = { nlpAction("Voir ma commande") })
                keyValue("Status", "In Delivery")
            }
            section("Location") {
                image("https://maps.googleapis.com/...") {
                    choiceAction(Intent("geoloc"))
                }
            }
            section {
                buttons {
                    textButton("OPEN ORDER") {
                        link("https://example.com/orders/...")
                    }
                    iconButton("https://example.com/resources/cancel") {
                        link("https://example.com/cancel/...")
                    }
                    iconButton(ChatIcon.PHONE) {
                        action("call", mapOf("number" to "+33671...", "country" to "FR"))
                    }
                }
            }
        }

    @Test
    internal fun `should build complete card`() {
        val googleMessageOut = buildCard().toGoogleMessage()
        assertThat(googleMessageOut).isNotNull
        assertThat(JacksonFactory().toPrettyString(googleMessageOut)).isEqualTo(
            """
            {
              "cards" : [ {
                "header" : {
                  "imageStyle" : "IMAGE",
                  "imageUrl" : "https://goo.gl/aeDtrS",
                  "subtitle" : "pizzabot@example.com",
                  "title" : "Pizza Bot Customer Support"
                },
                "sections" : [ {
                  "widgets" : [ {
                    "keyValue" : {
                      "content" : "12345",
                      "contentMultiline" : false,
                      "onClick" : {
                        "action" : {
                          "actionMethodName" : "SEND_SENTENCE",
                          "parameters" : [ {
                            "key" : "TEXT",
                            "value" : "Voir ma commande"
                          } ]
                        }
                      },
                      "topLabel" : "Order No."
                    }
                  }, {
                    "keyValue" : {
                      "content" : "In Delivery",
                      "contentMultiline" : false,
                      "topLabel" : "Status"
                    }
                  } ]
                }, {
                  "header" : "Location",
                  "widgets" : [ {
                    "image" : {
                      "imageUrl" : "https://maps.googleapis.com/...",
                      "onClick" : {
                        "action" : {
                          "actionMethodName" : "SEND_CHOICE",
                          "parameters" : [ {
                            "key" : "INTENT",
                            "value" : "geoloc"
                          } ]
                        }
                      }
                    }
                  } ]
                }, {
                  "widgets" : [ {
                    "buttons" : [ {
                      "textButton" : {
                        "onClick" : {
                          "openLink" : {
                            "url" : "https://example.com/orders/..."
                          }
                        },
                        "text" : "OPEN ORDER"
                      }
                    }, {
                      "imageButton" : {
                        "iconUrl" : "https://example.com/resources/cancel",
                        "onClick" : {
                          "openLink" : {
                            "url" : "https://example.com/cancel/..."
                          }
                        }
                      }
                    }, {
                      "imageButton" : {
                        "icon" : "PHONE",
                        "onClick" : {
                          "action" : {
                            "actionMethodName" : "call",
                            "parameters" : [ {
                              "key" : "number",
                              "value" : "+33671..."
                            }, {
                              "key" : "country",
                              "value" : "FR"
                            } ]
                          }
                        }
                      }
                    } ]
                  } ]
                } ]
              } ]
            }
            """.trimIndent(),
        )
    }

    @Test
    internal fun `should build and translate in french complete card`() {
        every { i18nTranslator.translate(text = any()) } answers {
            TranslatedString("Trad de ${arg<CharSequence>(0)}")
        }
        every { i18nTranslator.translate("Pizza Bot Customer Support") } returns TranslatedString("SAV Pizza Bot")

        val googleMessageOut = buildCard().toGoogleMessage()
        assertThat(googleMessageOut).isNotNull
        assertThat(JacksonFactory().toPrettyString(googleMessageOut)).isEqualTo(
            """
            {
              "cards" : [ {
                "header" : {
                  "imageStyle" : "IMAGE",
                  "imageUrl" : "https://goo.gl/aeDtrS",
                  "subtitle" : "Trad de pizzabot@example.com",
                  "title" : "SAV Pizza Bot"
                },
                "sections" : [ {
                  "widgets" : [ {
                    "keyValue" : {
                      "content" : "Trad de 12345",
                      "contentMultiline" : false,
                      "onClick" : {
                        "action" : {
                          "actionMethodName" : "SEND_SENTENCE",
                          "parameters" : [ {
                            "key" : "TEXT",
                            "value" : "Voir ma commande"
                          } ]
                        }
                      },
                      "topLabel" : "Trad de Order No."
                    }
                  }, {
                    "keyValue" : {
                      "content" : "Trad de In Delivery",
                      "contentMultiline" : false,
                      "topLabel" : "Trad de Status"
                    }
                  } ]
                }, {
                  "header" : "Trad de Location",
                  "widgets" : [ {
                    "image" : {
                      "imageUrl" : "https://maps.googleapis.com/...",
                      "onClick" : {
                        "action" : {
                          "actionMethodName" : "SEND_CHOICE",
                          "parameters" : [ {
                            "key" : "INTENT",
                            "value" : "geoloc"
                          } ]
                        }
                      }
                    }
                  } ]
                }, {
                  "widgets" : [ {
                    "buttons" : [ {
                      "textButton" : {
                        "onClick" : {
                          "openLink" : {
                            "url" : "https://example.com/orders/..."
                          }
                        },
                        "text" : "Trad de OPEN ORDER"
                      }
                    }, {
                      "imageButton" : {
                        "iconUrl" : "https://example.com/resources/cancel",
                        "onClick" : {
                          "openLink" : {
                            "url" : "https://example.com/cancel/..."
                          }
                        }
                      }
                    }, {
                      "imageButton" : {
                        "icon" : "PHONE",
                        "onClick" : {
                          "action" : {
                            "actionMethodName" : "call",
                            "parameters" : [ {
                              "key" : "number",
                              "value" : "+33671..."
                            }, {
                              "key" : "country",
                              "value" : "FR"
                            } ]
                          }
                        }
                      }
                    } ]
                  } ]
                } ]
              } ]
            }
            """.trimIndent(),
        )
    }
}
