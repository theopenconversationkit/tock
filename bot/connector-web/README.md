# Provides a Bot REST API

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

# Swagger

A simple [Swagger descriptor](./Swagger_TOCKWebConnector.yaml) of the rest service is provided¬.

# React chat widget

The [`tock-react-kit`](https://github.com/theopenconversationkit/tock-react-kit) component provides integration with
Web pages, customizable chat widgets and orchestration with a Web connector back-end.

# Extra headers

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
