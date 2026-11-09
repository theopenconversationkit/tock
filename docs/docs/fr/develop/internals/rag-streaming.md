---
title: Streaming des réponses RAG
description: "Comment Tock streame les réponses RAG vers les clients : contrat SSE, mode delta, activation par connecteur et données streamables."
---

# Streaming des réponses RAG

Cette page décrit comment la réponse d'un RAG est affichée progressivement, qui l'active, et ce qui peut (ou non) être
streamé. Elle complète la section [SSE et streaming du connecteur web](../../channels/web.md#server-sent-events-sse-et-streaming).

## Principe

Le streaming est une préoccupation de **transport et de présentation**, pas de **domaine**. La réponse du bot reste
**une seule action** (`SendSentenceWithFootnotes`), persistée une fois dans le dialogue. Les fragments de texte
n'existent que pour l'affichage progressif côté client : on n'éclate jamais la réponse en N actions partielles.

```
LLM (question_answering_llm)                            [orchestrateur Python]
  │ tokens
  ▼
POST /rag/stream → SSE : event "token" (×N), puis event "answer"
  │
  ▼
RAGServiceImpl.ragStream → listener.onToken / onAnswer  [client orchestrateur]
  │
  ▼
RAGAnswerHandler.ragStream                              [moteur]
  • onToken(delta) → callback.sendStreamedToken(delta)
  • onAnswer(resp) → SendSentenceWithFootnotes (1 action domaine)
  │
  ▼
Connecteur (web, REST, Google Chat)                     [affichage progressif puis réponse finale]
```

Le moteur expose un seul point d'extension : `ConnectorCallback.sendStreamedToken(content)`. Chaque connecteur décide
quoi en faire.

## Connecteur web : contrat sur le canal `/sse`

Avec `tock_web_sse=true`, le connecteur web n'utilise pas d'événement spécifique : il envoie des trames `message`
ordinaires sur le canal `<chemin>/sse?userid=<id>`, avec le contrat de streaming standard de Tock (le même que celui
du Bot API), consommé par `tock-react-kit` et `tock-vue-kit` :

- les métadonnées de chaque trame contiennent `RESPONSE_ID` et `TOCK_STREAM_RESPONSE=true` ;
- le client remplace le message portant le même `RESPONSE_ID` par le contenu de la trame ;
- la réponse finale (footnotes, boutons, etc.) garde `RESPONSE_ID` et `TOCK_STREAM_RESPONSE=true` et remplace le
  message streamé : **la réponse finale fait foi**.

Pour qu'une réponse streame, trois conditions sont nécessaires : `tock_web_sse=true`, un client dont le canal `/sse` est
connecté à l'instance qui traite le message, et un bot dont la story RAG est active. Dans ce cas le connecteur positionne
lui-même `metadata.streamedResponse=true` sur l'action utilisateur, ce qui fait choisir `ragStream` au moteur. Sinon, la
réponse complète est renvoyée dans la réponse du `POST`.

### Ordre des trames

Les trames partielles et la réponse finale sont écrites par le même contexte Vert.x (`runOnContext`, FIFO) : la réponse
finale part toujours après les trames déjà en file, et une trame partielle tardive est ignorée. Les tokens arrivent sur
un thread de l'orchestrateur ; c'est ce re-marshalling qui garantit l'ordre.

### Mode delta

Par défaut chaque trame contient le texte cumulé déjà rendu (markdown converti en HTML). Un client peut demander le mode
delta avec `"streamMode": "delta"` dans le corps de la requête. Les trames portent alors `TOCK_STREAM_MODE=delta` et
`TOCK_STREAM_KEEP=N` : le client garde les `N` premiers caractères du message courant et leur ajoute le texte de la
trame. Le serveur rend le texte comme en mode cumulé et n'envoie que la fin qui a changé (le HTML d'un markdown partiel
n'est pas un préfixe du suivant, d'où la règle « garder N caractères »). La réponse finale est toujours complète.

### Clients

| Client | Activation |
|---|---|
| `tock-react-kit` | consomme `/sse` automatiquement ; option `streamMode: 'delta'` |
| `tock-vue-kit` | `preferences.messages.streamedResponse: true` ; `preferences.messages.streamMode: 'delta'` |

Les deux ouvrent un `EventSource` sur `<chemin>/sse?userid=<id>`. Un navigateur limite à 6 le nombre de connexions
HTTP/1.1 par hôte : en développement, plusieurs onglets avec un `EventSource` ouvert peuvent bloquer les requêtes `POST`.

## Autres connecteurs

- **REST** : la route `<chemin>/:locale` ne streame jamais ; `<chemin>/:locale/stream` force
  `metadata.streamedResponse=true`.
- **Google Chat** : le paramètre `enableStreaming` (`"1"`) du connecteur, configuré dans le Studio, active le streaming
  pour tous les messages entrants.
- **Route `/sse/direct`** (`tock_web_direct_sse=true`) : route historique du connecteur web, qui streame la réponse
  d'un `POST` sur la même requête. Elle n'émet pas les fragments du RAG.

Ces connecteurs utilisent `sendStreamedToken` ; leur comportement n'est pas modifié par le streaming du connecteur web.

## Pas de réglage d'activation par bot

Le streaming ne change pas la réponse produite (mêmes footnotes, même persistance). Celui qui sait s'il peut streamer
est le client, pas le bot : un réglage « par bot » risquerait d'activer le streaming pour un appelant incapable de le
lire. L'activation est donc pilotée par la requête (canal connecté, route `/stream`) et encadrée par les interrupteurs
serveur (`tock_web_sse` en particulier). Les exécutions de datasets passent par la route REST de base : elles reçoivent
toujours la réponse finale complète. Ne jamais faire pointer le chemin dataset vers `/stream`.

## Ce qui est streamable

Le flux Python (`execute_rag_chain_stream`) a trois étapes :

```
1. rag_inputs.ainvoke()   → retrieval + condensation   (AVANT le streaming)
2. answer_chain.astream() → tokens du champ "answer"    (PENDANT le streaming)
3. build_rag_response()   → footnotes / obs / debug      (APRÈS le streaming)
```

- **Intrinsèquement terminal** (ne peut pas être streamé) : les footnotes (elles dépendent de `context_usage`, dernier
  champ du JSON `LLMAnswer`), la trace d'observabilité, `duration` / `step_durations`, ainsi que `status`,
  `redirection_intent`, `score` et `display_answer`.
- **Disponible tôt** (non streamé aujourd'hui) : les documents récupérés et les prompts de debug, connus dès l'étape 1.
- **Le texte** : déjà streamé.

Le texte progressif et la réponse finale peuvent différer (guardrail post-stream, troncature) : c'est pourquoi la réponse
finale remplace toujours l'affichage progressif. Les fragments ne sont pas persistés ; seule la réponse finale est
stockée.

## Variables d'environnement

| Variable | Défaut | Rôle |
|---|---|---|
| `tock_web_sse` | `false` | Active le canal SSE persistant, et donc le streaming des réponses du connecteur web |
| `tock_web_direct_sse` | `false` | Active la route `<chemin>/sse/direct` |
| `tock_web_connector_merge_stream_response` | `true` | Fusionne les `SendSentence` partiels du streaming Bot API en une réponse (sans effet sur le streaming RAG) |
| `tock_web_sse_keepalive_delay` | `10` | Secondes entre deux pings SSE |
