# Activation du streaming RAG : choix d'architecture

> Document d'architecture — explique **comment on active/désactive** le streaming de la
> réponse RAG, et **pourquoi** on n'a pas ajouté de réglage dédié par bot (type
> `debugEnabled`). Complète [`docs-streaming-rag-canal-token.md`](./docs-streaming-rag-canal-token.md).

## Décision

**On ne rajoute aucun réglage d'activation dédié (ni par bot, ni par connecteur).**
Le streaming est **piloté par la requête entrante**, encadré par des **interrupteurs
serveur** (variables d'environnement). Ce contrôle multi-niveaux existant suffit ; un
toggle `streamingEnabled` sur la configuration RAG ferait doublon.

## Pourquoi ce choix

Le streaming n'est **pas** une propriété du *domaine* (comme le serait `debugEnabled`,
qui change la *réponse* produite). C'est une propriété du **transport** : afficher le
texte progressivement ou d'un bloc ne change **pas** la réponse finale (même
`SendSentenceWithFootnotes`, mêmes footnotes, même persistance — cf. le canal `token`).

Donc :

1. **Celui qui sait s'il peut streamer, c'est le client.** Un front SSE sait consommer un
   flux ; un client non-SSE, un batch, un dataset, non. C'est donc au **client/à la route**
   de demander le streaming, pas au bot de l'imposer.
2. **On ne doit jamais *forcer* du streaming.** Un toggle « par bot » façon `debugEnabled`
   (combiné en OU) risquerait d'activer le streaming pour un appelant incapable de le lire.
   La bonne sémantique serait un ET (`requête ET bot`), ce qui n'apporte qu'un
   interrupteur maître… que les variables d'env fournissent déjà côté serveur.
3. **Le besoin « pas de streaming pendant les datasets » est déjà satisfait** par le
   routage (voir plus bas), sans aucun réglage.

## Les niveaux de contrôle existants

Le streaming se décide à **trois niveaux**, du plus global au plus fin.

### 1. Interrupteurs serveur (variables d'environnement)

Ces variables conditionnent l'**existence même** des routes/mécanismes SSE. Si elles sont
à `false`, aucun client ne peut streamer, quels que soient les flags de requête.

| Variable | Défaut | Rôle | Où |
|---|---|---|---|
| `tock_web_sse` | `false` | Active l'endpoint SSE « canal persistant » `"$path/sse"` (file de messages via MongoDB, notifications push). Contrôle aussi `notify()` dans les connecteurs Web et OpenAI. | `WebConnector.kt:90`, `OpenAIConnector.kt:67` |
| `tock_web_direct_sse` | `false` | Active l'endpoint SSE **direct** `"$path/sse/direct"` : le flux est poussé directement sur la réponse HTTP (sans file Mongo). **C'est la route utilisée pour le streaming RAG de bout en bout.** | `WebConnector.kt:91` |
| `tock_web_connector_merge_stream_response` | `true` | Fusionne les `SendSentence` partiels du mécanisme natif `sendStreamedResponse` en une seule réponse. Sans effet sur le canal `token` (RAG streaming). | `WebConnectorCallback.kt:37` |
| `tock_web_sse_keepalive_delay` | `10` (s) | Intervalle des pings keep-alive sur toute réponse SSE. | `VertXs.kt:59` |
| `tock_openai_support_unstreamed` | `false` | (Connecteur OpenAI) quand `false` (défaut), les requêtes chat sans streaming (`stream=false`) sont **silencieusement ignorées** (200 vide). Mettre à `true` pour les traiter normalement. | `OpenAIConnector.kt:68` |

> Le CORS n'autorise la méthode `GET` sur les routes du connecteur web que si
> `tock_web_sse` **ou** `tock_web_direct_sse` est actif (`WebConnector.kt:138`).

### 2. Déclenchement par la requête entrante (opt-in client/config)

- **Connecteur web** : champ booléen `streamedResponse` dans le **corps JSON** de la
  requête (`WebConnectorRequest.streamedResponse`, recopié dans
  `ActionMetadata.streamedResponse` par `toEvent(...)`).
- **Connecteur REST** : c'est la **route** qui décide.
  - `"$path/:locale"` (base) → jamais de streaming (`streamedResponse` reste `false`).
  - `"$path/:locale/stream"` → `action.metadata.streamedResponse = true` (forcé).
- **Connecteur Google Chat** : champ `enableStreaming` configuré dans l'**UI admin Studio**
  (`GoogleChatConnectorProvider.kt:54`, `ENABLE_STREAMING_PARAMETER = "enableStreaming"`).
  Valeur `"1"` pour activer. Quand activé, **tous les messages entrants** de ce connecteur
  reçoivent `event.metadata.streamedResponse = true` (`GoogleChatConnector.kt:106-108`).
  À la différence de Web/REST, il n'y a pas d'opt-in par requête : c'est une configuration
  permanente de l'instance du connecteur.

> Il n'y a **ni header ni query param** pour le streaming : corps JSON (web), route (REST),
> ou configuration admin (Google Chat).

### 3. Décision dans le moteur (`RAGAnswerHandler`)

```kotlin
// Streaming is opt-in by the incoming request only (SSE streaming route / connector).
return if (botBus.action.metadata.streamedResponse) {
    ragStream(botBus)
} else {
    ragClassic(botBus)
}
```

Le moteur ne fait que **suivre** le flag de l'action. Il ne connaît aucun réglage
d'activation propre : c'est précisément le choix d'architecture de ce document.

## Cas des datasets : déjà non-streamés

Le runner de dataset n'emprunte **jamais** une route de streaming :

```
DatasetRunWorker → TestTalkService.talk(..., debugEnabled = true, sourceWithContent = true)
  → ConnectorRestClient.talk(...)        // @POST("{locale}")  ← route REST de BASE
  → RestConnector (route "$path/:locale") // streamedResponse reste false
  → RAGAnswerHandler.rag() → ragClassic   // réponse finale unique
```

`TestTalkService`/`ClientMessageRequest` n'exposent même pas de champ `streamedResponse`,
et la route de base ne le positionne jamais. **Les runs de dataset reçoivent donc
toujours la réponse finale complète**, sans streaming — exactement le comportement voulu,
garanti par le routage et non par un réglage.

> Seul garde-fou à conserver : ne jamais faire pointer le chemin dataset vers la route
> `/stream` ni ajouter `streamedResponse=true` à `ClientMessageRequest` côté test.

## Récapitulatif

| Je veux… | Levier |
|---|---|
| Couper le streaming pour **tout le serveur** | `tock_web_sse=false` et `tock_web_direct_sse=false` |
| Streamer **cette interaction** (web) | `streamedResponse: true` dans le corps JSON |
| Streamer **cette interaction** (REST) | appeler la route `"$path/:locale/stream"` |
| Streamer **toutes les interactions** (Google Chat) | configurer `enableStreaming = "1"` dans l'UI admin Studio pour le connecteur |
| Juste la **réponse finale** (dataset, batch, client non-SSE) | ne rien demander : route de base / `streamedResponse=false` (comportement par défaut) |

**En résumé** : le contrôle d'activation est déjà complet (serveur → requête → routage).
Ajouter un `streamingEnabled` par bot n'apporterait qu'un doublon de l'interrupteur
serveur, pour une préoccupation (le transport) qui n'a pas sa place dans la config de
domaine de la RAG.
