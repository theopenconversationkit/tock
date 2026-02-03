# Tock Bot Web Connector SSE

This module provides [Server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events) capabilities for the Tock Web Connector,
allowing web clients to establish persistent connections and receive bot responses in real-time.
Works with multi-instance deployments, with a message queue implementation backed by TOCK's MongoDB database.

If a message is sent while the corresponding user is offline, it will be kept in the MongoDB database and delivered upon reconnection.

## Connection Flow

1. **Connection Establishment**: Client connects to SSE endpoint with user identification
    - user identification may be handled by a custom `WebSecurityHandler`, otherwise defaults to query param `userId`
2. **Channel Registration**: Server creates an SSE channel and registers callback for message delivery
3. **Message Routing**: 
   - Local delivery if user is connected to current instance
   - MongoDB persistence if user is offline or on different instance
4. **Change Stream Synchronization**: Other instances receive messages via MongoDB change streams
5. **Missed Events**: Upon reconnection, undelivered messages are sent to the client

## Usage Samples

### Setting Up the Endpoint

```kotlin
val endpoint = SseEndpoint()
val router = Router.router(vertx)

endpoint.configureRoute(
    router = router,
    path = "/api/bot/sse",
    connectorId = "my-web-connector",
    webSecurityHandler = WebSecurityCookiesHandler(), // or any other implementation
)
```

### Client Connection

Connect to the SSE endpoint from a browser:

```javascript
const eventSource = new EventSource('/api/bot/sse');

eventSource.onmessage = (event) => {
    const response = JSON.parse(event.data);
    console.log('Bot response:', response);
};
```

### Sending Responses

```kotlin
endpoint.sendResponse(
    connectorId = "my-web-connector",
    recipientId = userId,
    response = WebConnectorResponseContent(responses = listOf(WebMessageContent(text = "Hello, World!")))
)
```
