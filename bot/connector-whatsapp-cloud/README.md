> **This connector is still in alpha phase of development**
> 
> Breaking changes may occur.

## Prerequisites

* You need to retrieve from Meta these values:

    * **WhatsApp Phone Number id**: The WhatsApp id of the phone number from which the bot should send messages.
    * **WhatsApp Business Account id**: The id of the WhatApp business account on which the app is registered.
    * **Webhook verify token**: A token (choose what you want) used when registering the webhook in the Meta admin interface.
    * **Call Token**: The token allowing the bot to send messages.
    * (Optional) **Meta Application id**: The id of the Meta application, only required for [template management](#template-management).

* Then go to the *Configuration* -> *Bot Configurations* menu in Tock Studio, and create a new configuration with these parameters.
  Set the `Mode` field to "subscribe".

## Bot API

* Take the bot demo url (ie  https://demo.tock.ai ) and use it in the webhook interface of WhatsApp Cloud settings, to specify:
    * the url : https://demo.tock.ai/io/xxxx/new_assistant/whatsapp (where */io/xxxx/new_assistant/whatsapp* is the relative path of the connector)
    * the webhook token you set

-> The bot is ready!

## Integrated mode

In order to connect your bot with a WhatsApp bot application, you need a Meta application with "messages" webhook events activated (look at the [Meta documentation](https://developers.facebook.com/docs/whatsapp/webhooks)).

* A secure ssl tunnel (for example [zrok](https://zrok.io/)) is required to test the bot in dev mode:

```sh 
    zrok share public 8080
``` 

* Take the zrok value (ie  https://xxxx.share.zrok.io ) and use it in the webhook interface of whatsapp settings, to specify :
    * the url : https://xxxx.share.zrok.io/whatsapp (where `/whatsapp` is the relative path of the connector)
    * the webhook token you set

* The documentation of the whatsapp builders is available in [KDoc format](https://theopenconversationkit.github.io/tock/dokka/tock/ai.tock.bot.connector.whatsapp.cloud/index.html)

-> The bot is ready !

## Proxy authentication

TOCK uses the default proxy selector for calls to the WhatsApp API.
If the bot is deployed behind an HTTP proxy that requires authentication to perform these calls,
you will need to set credentials in the form of the `tock_proxy_user` and `tock_proxy_password` properties (only Basic auth is supported).
These properties can be set either as Java system properties or as environment variables.

## Message limitations

Whatsapp message factory methods will ensure the length of texts in messages is acceptable according to the WhatsApp API specification.
By default, if a text is too long, it will be truncated and a warning will be logged. Similarly,
if a message contains too many buttons, only the first ones will be displayed.
However, you can set the `tock_whatsapp_error_on_invalid_messages`
property to `true` to throw an error instead, which can notably be helpful when running automated tests.

## Template Management

This connector provides an experimental [template management](https://developers.facebook.com/docs/whatsapp/business-management-api/message-templates/)
feature, eliminating the need to manually create templates
in the business management console before being able to send them.

This feature can be enabled by setting the `tock_whatsapp_sync_templates` property to `true`,
and providing at least one `WhatsappTemplateProvider` implementation.
Only the WhatsApp connectors with a set *Meta Application Id* property will trigger a template sync
on their corresponding business account.

**Warning**: WhatsApp templates incur a cost for every message sent.
Check the [pricing page](https://developers.facebook.com/docs/whatsapp/pricing/)
to determine if this makes sense for your application.

### WhatsApp Template Provider Example
```kotlin
class TutorialWhatsappTemplateProvider : WhatsappTemplateProvider {
    companion object {
        const val TEMPLATE_ID = "tutorial_carousel"
        private val supportedLanguages = setOf(Locale.ENGLISH, Locale.FRENCH)
    }

    override fun createTemplates(ctx: TemplateGenerationContext): List<WhatsappTemplate> {
        return supportedLanguages.map { lang ->
            ctx.createTutorialTemplate(lang)
        }
    }

    private fun TemplateGenerationContext.createTutorialTemplate(locale: Locale): WhatsappTemplate {
        return buildCarousel(TEMPLATE_ID, locale) {
            body = TemplateBody(translate(TutorialTexts.MSG_1))
            carousel = TemplateCarousel(
                TutorialCard.entries.map { tutorialCard ->
                    TemplateCard(
                        TemplateCardHeader(HeaderFormat.IMAGE, getOrUpload(
                            tutorialCard.imageUrl,
                            "image/png",
                        )),
                        TemplateCardBody(translate(tutorialCard.text)),
                        TemplateQuickReply(translate(tutorialCard.qr1Title)),
                        TemplateQuickReply(translate(tutorialCard.qr2Title)),
                    )
                }
            )
        }
    }
}
```

This template can then be sent like so:
```kotlin
send {
    whatsAppCloudBuildTemplateMessageCarousel(
        TutorialWhatsappTemplateProvider.TEMPLATE_ID,
        TutorialCard.entries.mapIndexed { idx, tutorialCard ->
            whatsAppCloudCardCarousel(
                idx,
                listOf(
                    whatsAppCloudHeaderTemplate(
                        ParameterType.IMAGE.name,
                        tutorialCard.imageUrl,
                    ),
                    whatsAppCloudPostbackButton(
                        index = 0,
                        title = tutorialCard.qr1Title,
                        targetIntent = tutorialCard.qr1Story,
                    ),
                    whatsAppCloudPostbackButton(
                        index = 1,
                        title = tutorialCard.qr2Title,
                        targetIntent = tutorialCard.qr2Story,
                    ),
                ),
            )
        },
        userLocale.language,
    )
}
```
