Provides a Bot REST API

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