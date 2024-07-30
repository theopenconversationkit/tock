# TOCK Deepl Translation

Here are the configurable variables:

- `tock_translator_deepl_target_languages`: set of supported languages - ex : en,es
- `tock_translator_deepl_api_url`: Deepl api url (default pro api url : https://api.deepl.com/v2/translate). 
  If you have problems with pro api, you can use free api : https://api-free.deepl.com/v2/translate
- `tock_translator_deepl_api_key` : Deepl api key to use (see your account)
- `tock_translator_deepl_glossary_id`: glossary identifier to use in translation

> Deepl documentation: https://developers.deepl.com/docs

To integrate the module into a custom Tock Admin, pass the module as a parameter to the `ai.tock.nlp.admin.startAdminServer()` function.

Example:

```kt
package ai.tock.bot.admin

import ai.tock.nlp.admin.startAdminServer
import ai.tock.translator.deepl.deeplTranslatorModule

fun main() {
    startAdminServer(deeplTranslatorModule())
}
```

## Http Client Configuration

You can configure the Deepl client, including proxy settings, by passing a parameter to `deeplTranslatorModule`:

```kt
startAdminServer(deeplTranslatorModule(OkHttpDeeplClient {
    proxyAuthenticator { _: Route?, response: Response ->
        // https://square.github.io/okhttp/3.x/okhttp/index.html?okhttp3/Authenticator.html
        if (response.challenges().any { it.scheme.equals("OkHttp-Preemptive", ignoreCase = true) }) {
            response.request.newBuilder()
                .header("Proxy-Authorization", credential)
                .build()
        } else {
            null
        }
    }
}))
```
