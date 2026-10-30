# Tock Vue Kit — Intégration du streaming RAG

> Document d'implémentation — explique **comment le TVK (Tock Vue Kit) consomme le streaming**
> de réponses RAG depuis le backend Tock, gère les événements SSE, et affiche les textes
> progressivement.

---

## 1. Vue d'ensemble

Le streaming RAG fonctionne selon ce flux complet :

```
Backend Tock (LLM générant tokens)
  ↓ (POST /sse/direct + streamedResponse=true)
  ↓
TVK (fetch SSE + ReadableStream)
  ├─ event: token (×N)      → accumule texte progressif
  ├─ event: message/answer  → texte final + footnotes + structure
  └─ metadata: LAST_ANSWER  → signal de fin
  ↓
Vue Component (message-text.vue)
  ├─ Affiche curseur clignotant pendant streaming
  ├─ Accumule tokens dans message.text
  └─ Remplace avec texte final quand arrive
```

**Clé** : Les tokens assurent l'**affichage progressif** (UX), l'event `message` final est la
**source de vérité** (domaine).

---

## 2. Architecture TVK — côté store

**Fichier** : `src/stores/main-state.ts`

### 2.1 Point d'entrée — `sendUserMessage()` (ligne 377)

Décision `streamedResponse` :

```typescript
if (appOptions.preferences.messages.streamedResponse) {
  // SSE streaming mode via /sse/direct
  await handleStreamedResponseDirect(...)
} else {
  // Classique buffered via POST
  const query = await fetch(tockEndPoint!, { ... })
  await handleBufferedResponse(query, mainStoreInstance)
}
```

### 2.2 Chemin streaming — `handleStreamedResponseDirect()` (ligne 192)

Prépare la requête et envoie vers `/sse/direct` :

```typescript
const payload: TockQuery = {
  query: message,
  userId: userId,
  locale: locale,
  sourceWithContent: appOptions.preferences.messages.footNotes.requireSourcesContent,
  streamedResponse: true,  // ← Signal au backend pour streaming
};

const ssePath = tockEndPoint.replace(/\/$/, "") + "/sse/direct";
const response = await fetch(ssePath, {
  method: "POST",
  body: JSON.stringify(payload),
  headers: { "Content-Type": "application/json" },
});

await handleStreamedResponse(response, mainStoreInstance);
```

**Backend** : 
- Route : `WebConnector.kt:168` — `POST <path>/sse/direct`
- Action déclenchée : `RAGAnswerHandler.ragStream()` (car `streamedResponse=true`)
- Répond en SSE vers `/rag/stream` du serveur Python

---

### 2.3 Parsing SSE — `handleStreamedResponse()` (ligne 244)

#### Étape 1 : Créer message en streaming (lignes 250-257)

```typescript
const streamingMsg: Message = {
  type: MessageType.message,
  author: MessageAuthor.bot,
  text: "",               // Texte accumulé progressif
  streaming: true,        // ← Affiche le curseur
  date: Date.now(),
};
mainStoreInstance.addMessage(streamingMsg);
const reactiveMsg = state.value.messages[state.value.messages.length - 1];
```

#### Étape 2 : Boucle de lecture SSE (lignes 272-363)

```typescript
const reader = response.body.getReader();
const decoder = new TextDecoder();
let buffer = "";
let finished = false;

while (!finished) {
  const { done, value } = await reader.read();
  if (done) break;
  
  buffer += decoder.decode(value, { stream: true });
  const events = buffer.split("\n\n");  // ← Séparation des frames SSE
  buffer = events.pop() || "";
  
  for (const rawEvent of events) {
    // Parsing bas-niveau : récupérer type et data
    const lines = rawEvent.split("\n");
    let eventType = "";
    let data = "";
    for (const line of lines) {
      if (line.startsWith("event: ")) eventType = line.slice(7);
      if (line.startsWith("data: ")) data = line.slice(6);
    }
    
    // Traiter token progressif
    if (eventType === "token" && data) {
      const tok = JSON.parse(data);
      if (typeof tok.content === "string") {
        reactiveMsg.text += tok.content;  // ← Accumule
        mainStoreInstance.scrollMessages();
      }
      continue;
    }
    
    // Traiter message/answer final
    if (eventType === "message" && data) {
      const res = JSON.parse(data);
      if (res.responses?.length > 0) {
        const first = res.responses[0];
        reactiveMsg.text = first.text;           // ← Remplace par texte final
        reactiveMsg.buttons = first.buttons;
        reactiveMsg.footnotes = first.footnotes;
      }
      
      // Déterminer fin du stream
      if (res.metadata?.LAST_ANSWER === "true") {
        reactiveMsg.streaming = false;           // ← Curseur disparu
        await reader.cancel();
        finished = true;
        break;
      }
    }
  }
}
```

---

## 3. Flux des événements — Mapping backend → front

### 3.1 Backend envoie tokens

**Endpoint** : `POST /rag/stream` (orchestrateur Python)

```
Orchestrator sends:
  ← event: token
    data: {"content": "Bonjour "}
  ← event: token
    data: {"content": "le "}
  ← event: token
    data: {"content": "monde"}
  ← event: answer (terminal)
    data: {
      "responses": [{
        "text": "Bonjour le monde",     ← texte complet
        "footnotes": [...],
        "buttons": [...]
      }],
      "metadata": { "LAST_ANSWER": "true" }
    }
```

**Origine** : `RAGAnswerHandler.ragStream()` (`RAGAnswerHandler.kt:276`)
- Appelle `ragService.ragStream(listener)` qui reçoit tokens
- Chaque token via `listener.onToken()` → `callback.sendStreamedToken()` (`WebConnectorCallback.kt:102`)
- Réponse finale via `listener.onAnswer()` → `bus.send(SendSentenceWithFootnotes)`

### 3.2 TVK accumule et affiche

```
Reçoit event: token "Bonjour " → reactiveMsg.text = "Bonjour "
Reçoit event: token "le "     → reactiveMsg.text = "Bonjour le "
Reçoit event: token "monde"   → reactiveMsg.text = "Bonjour le monde"
Reçoit event: answer          → reactiveMsg.text = "Bonjour le monde" (inchangé ou rewrite)
                              → reactiveMsg.footnotes = [...]
                              → reactiveMsg.streaming = false
                              → Curseur disparu
```

---

## 4. Métadonnées de contrôle

### 4.1 `LAST_ANSWER` (terminal signal)

**Envoyé par** : `WebConnectorCallback.addMetadata(MetadataEvent.lastAnswer(...))`
(`WebConnectorCallback.kt:267`)

**Valeur** : `"true"` (string)

**Rôle** : Signale au TVK que l'event `message`/`answer` qui porte cette métadonnée est le
dernier. Arrête la lecture SSE et met `streaming = false`.

**Cas de perte** :
- Si LAST_ANSWER ne vient jamais → TVK attend indéfiniment (👉 Timeout à 60s requis)
- Si connection coupe → TVK détecte `reader.read() = done` et sort

### 4.2 `streamedResponse` (requête)

**Envoyé par** : TVK dans `TockQuery` (ligne 209)

```typescript
const payload: TockQuery = {
  query: message,
  userId: userId,
  locale: locale,
  streamedResponse: true,  // ← Indique "je veux du streaming"
};
```

**Backend** : `WebConnectorCallback` lit ce flag (ligne 263 WebConnector.kt) et le passe à
`ActionMetadata.streamedResponse`. `RAGAnswerHandler.rag()` route sur `ragStream()` ou
`ragClassic()` selon ce flag.

---

## 5. Cas d'erreur et timeouts

### 5.1 Timeout SSE

**Problème** : Les connexions SSE peuvent rester ouvertes indéfiniment si LAST_ANSWER ne vient
jamais (backend hang, error non détectée, etc.).

**Côté backend** : `tock_gen_ai_orchestrator_client_request_timeout_ms` = 55 000 ms (défaut).
L'orchestrateur abandonne après 55s.

**Côté TVK** : 🔴 **ACTUELLEMENT AUCUN TIMEOUT**. À ajouter : 60s timeout sur `handleStreamedResponse()`.

**Implémentation** :
```typescript
const STREAMING_TIMEOUT_MS = 60000;
const timeoutHandle = setTimeout(() => {
  console.warn("🔄 SSE: Streaming timeout after 60s");
  reader.cancel();
  finished = true;
  reactiveMsg.streaming = false;
  if (!reactiveMsg.text) {
    reactiveMsg.text = "[Response timeout - no data received]";
  }
}, STREAMING_TIMEOUT_MS);

try {
  // ... main loop
} finally {
  clearTimeout(timeoutHandle);
}
```

### 5.2 Déconnexion mid-stream

Si la connexion se coupe avant LAST_ANSWER :

```typescript
const { done, value } = await reader.read();
if (done) {
  console.log("🔄 SSE: Stream ended prematurely (no LAST_ANSWER)");
  reactiveMsg.streaming = false;
  // reactiveMsg.text contient tout ce qui a été accumulé via tokens
  break;
}
```

Le texte partiel reste visible — correct, c'est ce qui s'est reçu.

### 5.3 Divergence token ≠ texte final

**Cas** : Guardrails ou réécriture côté backend modifient le texte final.

**Exemple** :
```
Tokens accumulés : "Voici un mauvais mot: xyz"
Texte final       : "Voici une bonne phrase: abc"
```

Le TVK détecte ce cas et remplace (ligne 335) :
```typescript
reactiveMsg.text = first.text;  // ← Remplace par source de vérité
```

C'est correct, mais crée un "flash" visuel. À logger pour debug :

```typescript
const tokenAccumulated = reactiveMsg.text;
if (tokenAccumulated !== first.text && tokenAccumulated.length > 0) {
  console.warn(
    "🔄 SSE: Text rewrite detected (guardrails/condensation):",
    { 
      fromTokens: tokenAccumulated.length, 
      final: first.text.length 
    }
  );
}
reactiveMsg.text = first.text;
```

---

## 6. Configuration et prérequis

### 6.1 Backend

**Fichier** : `.env` ou système de configuration Tock

```bash
# Activer le streaming SSE direct
tock_web_direct_sse=true

# RAG doit être activé sur le bot
# (sinon la requête ne route pas vers streaming)
```

**Bot** : La story RAG doit être activée sur l'instance du bot.

### 6.2 TVK

**Fichier** : `src/utils/app-options-model.ts` (ligne 91)

```typescript
streamedResponse: {
  title: "Streamed response",
  type: "boolean",
  default: false,
  description:
    "When enabled, bot responses are streamed via SSE and displayed progressively. " +
    "⚠️ Requires: (1) tock_web_direct_sse=true on server, " +
    "(2) RAG enabled on bot, (3) ReadableStream browser support. " +
    "Timeout: 60s.",
  index: 23,
},
```

### 6.3 Client HTTP

TVK utilise `fetch()` + `ReadableStream` (natif browsers modernes).

**Support** : Tous les navigateurs modernes (Edge 79+, Chrome 89+, Firefox 102+, Safari 14.1+).

---

## 7. Diagramme de flux

```
┌─────────────────────────────────────────────────────────────────┐
│  User sends message (TVK)                                       │
│  └─ if streamedResponse enabled                                │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
          ┌──────────────────────────────┐
          │ POST /sse/direct             │
          │ body: {                      │
          │   query, userId, locale,     │
          │   streamedResponse: true     │
          │ }                            │
          └────────────┬─────────────────┘
                       │
                       ▼ (WebConnector.kt:168)
          ┌──────────────────────────────┐
          │ Backend: ActionMetadata      │
          │ streamedResponse = true      │
          └────────────┬─────────────────┘
                       │
                       ▼ (RAGAnswerHandler.rag())
          ┌──────────────────────────────┐
          │ → ragStream() path           │
          │ → BackgroundEventSource      │
          │ → POST /rag/stream (Python)  │
          └────────────┬─────────────────┘
                       │
      ┌────────────────┼────────────────┐
      │                │                │
      ▼                ▼                ▼
  event: token    event: token      event: answer
  data: {         data: {           data: {
    content: "B"    content: "onjour"  responses: [{
  }               }                   text: "Bonjour...",
                                      footnotes: [...],
                                      buttons: [...]
                                    }],
                                    metadata: {
                                      LAST_ANSWER: "true"
                                    }
                                  }
      │                │                │
      └────────────────┼────────────────┘
                       │
                       ▼ (TVK: handleStreamedResponse)
          ┌──────────────────────────────┐
          │ Token handlers:              │
          │ - Accumulate text            │
          │ - Show cursor                │
          │ - Scroll messages            │
          │                              │
          │ Message handler:             │
          │ - Replace text (if rewrite)  │
          │ - Set footnotes/buttons      │
          │ - Detect LAST_ANSWER         │
          │ - Hide cursor                │
          └──────────────────────────────┘
```

---

## 8. Points de débogage

### 8.1 Logs TVK

Tous les logs streaming sont préfixés `🔄 SSE:` ou `📦 Buffered:`.

```javascript
// Voir dans la console du navigateur :
🔄 SSE: Starting streaming response handler
🔄 SSE: Received chunk 1, buffer length: 256
🔄 SSE: Event type: token
🔄 SSE: Updated text length: 42
🔄 SSE: lastAnswer detected, stopping stream
🔄 SSE: Stream completed successfully
```

### 8.2 Vérifier LAST_ANSWER

Dans DevTools réseau, regarder la réponse SSE brute :

```
:comment
event: token
data: {"content":"..."}

event: message
data: {"responses":[...],"metadata":{"LAST_ANSWER":"true"}}
```

Si `LAST_ANSWER` manque → TVK attend timeout (60s).

### 8.3 Comparer token vs final

Si texte progessif ≠ texte final, voir log :

```
🔄 SSE: Text divergence (guardrails/rewrite):
  { tokenAccumulated: 156, final: 142 }
```

---

## 9. Références croisées

- **Backend streaming RAG** : `docs-interrogation-bot-rag.md` § 4.4
- **Activation streaming** : `docs-streaming-rag-activation.md`
- **Canal token vs message** : `docs-streaming-rag-canal-token.md`
- **WebConnector routes** : `docs-interrogation-bot-rag.md` § 5.3
