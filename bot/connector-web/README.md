# TOCK Web Connector

## Bot REST API usage

1) Add a web connector in admin interface with /test relative path.

2) Send text: `curl localhost:8080/test --data '{"query":"Hello","userId":"id"}'`

response:
```
{
  "responses": [
    {
      "text": "Welcome to the Tock Open Data Bot! :)"
    },
    {
      "text": "This is a Tock framework demonstration bot: https://github.com/theopenconversationkit/tock"
    },
    {
      "text": "Hey!",
      "buttons": [
        {
          "title": "Itineraries",
          "payload": "search?_previous_intent=greetings"
        },
        {
          "title": "Departures",
          "payload": "departures?_previous_intent=greetings"
        },
        {
          "title": "Arrivals",
          "payload": "arrivals?_previous_intent=greetings"
        }
      ]
    }
  ]
}
```

3) Select a button: `curl localhost:8080/test --data '{"payload":"search?_previous_intent=greetings","userId":"id"}'`
```
{
  "responses": [
    {
      "text": "For which destination?"
    }
  ]
}
```

4) Get a card:

```
{
  "responses": [
    {
      "card": {
          {
            "title": "Title",
            "subTitle": "subTitle",
            "file": {
              "url": "https:/url1",
              "name": "name",
              "type": "image"
            },
            "actions": [
              {
                "title": "Test"
              },
              {
                "title": "Test2"
              }
            ]
          }
    }
  ]
}
```

5) Get a carousel:
```
{
  "responses": [
    {
      "carousel": {
        "cards": [
          {
            "title": "Title",
            "subTitle": "subTitle",
            "file": {
              "url": "https:/url1",
              "name": "name",
              "type": "image"
            },
            "actions": [
              {
                "title": "Test"
              },
              {
                "title": "Test2"
              }
            ]
          },
          {
            "title": "Title2",
            "subTitle": "subTitle2",
            "file": {
              "url": "https:/url2",
              "name": "name2",
              "type": "image2"
            },
            "actions": [
              {
                "title": "Test"
              },
              {
                "title": "Test2"
              }
            ]
          }
        ]
      }
    }
  ]
}
```

### Swagger

A simple [Swagger descriptor](./Swagger_TOCKWebConnector.yaml) of the rest service is provided¬.

## Server-sent events (SSE)

This connector supports sending messages using [Server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events).
This feature can be enabled by setting the `tock_web_sse` optional property to `true`.
When SSE is enabled, bot answers will be sent both as POST responses and as new events in the event stream -
the [tock-react-kit](https://github.com/theopenconversationkit/tock-react-kit#sse) is configured to ignore the former
while it is listening to server-sent events.

The `tock_web_sse_keepalive_delay` optional property can be used to configure the number of seconds between
two SSE pings (default: 10).

## React chat widget

The [`tock-react-kit`](https://github.com/theopenconversationkit/tock-react-kit) component provides integration with
Web pages, customizable chat widgets and orchestration with a Web connector back-end.

To send a custom widget, make a class implementing `WebWidget` and override the `data` property with one of
the desired type, which must serialize to a JSON object.

For example:
```kotlin
data class TrainCardWidget(override val data: TrainCardProps) : WebWidget

data class TrainCardProps(val departure: String, val arrival: String)
```

When sending messages using the [SSE](#server-sent-events-sse) feature, widgets must also be deserializable by KMongo.
This requires registering the widget's class in the application's bootstrap:
```kotlin
KMongoConfiguration.bsonMapper.subtypeResolver.registerSubtypes(
    TrainCardWidget::class.java,
)
```

## Extra headers

Sometimes it is useful to allow extra HTTP headers to bot requests, for instance to provide/pass authentication or 
custom parameters from the front-end.

Not-allowed headers can cause [_CORS_](https://en.wikipedia.org/wiki/Cross-origin_resource_sharing) 
errors on the front-end.

The HTTP parameters, allowed by default can be found in [`WebConnector`](./src/main/kotlin/WebConnector.kt) source code.

To allow extra parameters without modifying the Web connector, use the optional `tock_web_connector_extra_headers` 
property.

Docker-Compose example:

```
version: '3'
services:
bot_api:
  image: tock/bot_api:$TAG
  environment:
    - tock_web_connector_extra_headers=header1,header2,my-other-header-param
```

> To add extra headers from a [`tock-react-kit`](https://github.com/theopenconversationkit/tock-react-kit) front-end, 
> refer to its [README#extra-headers](https://github.com/theopenconversationkit/tock-react-kit#extra-headers).

# Cookie storage for userId

By default, a user's unique identifier is generated and stored by the client as an arbitrary string,
which is passed to the web connector through the HTTP body and/or through query parameters.
Setting the `tock_web_cookie_auth` environment variable to `true`
makes it so the server stores users' identifiers in a secure, HTTP-only cookie, generating random unique identifiers
(using the UUID V4 format) if no such cookie is found.
