# Le canal `token` : streaming RAG hors-bande

> Document d'architecture — explique pourquoi le streaming de la réponse RAG
> passe par un **canal dédié** (`ConnectorCallback.sendStreamedToken`) plutôt que
> par le mécanisme natif de streaming d'actions de Tock.

## TL;DR

Le streaming est une préoccupation de **transport/présentation**, pas de **domaine**.
La réponse du bot reste **une seule action** (`SendSentenceWithFootnotes`). Les tokens
partent **hors-bande** (canal `token`) uniquement pour l'affichage progressif côté
client. On n'éclate jamais la réponse en N actions partielles.

---

## Les deux mécanismes de streaming

Tock expose aujourd'hui **deux** façons de streamer vers un client SSE.

### 1. `sendStreamedResponse(action: Action)` — mécanisme natif Tock (préexistant)

```kotlin
fun sendStreamedResponse(action: Action) {
    context?.response()?.sendSseMessage(webMapper.writeValueAsString(createResponse(listOf(action))))
}
```

- Entrée : une **`Action` complète** (`SendSentence`, `SendSentenceWithFootnotes`…).
- Sortie : une frame SSE `event: message` contenant un `WebConnectorResponse`
  (format métier complet : messages structurés, footnotes, metadata).
- Pour streamer un texte, il faut produire **plusieurs `SendSentence` partiels** comme
  autant d'actions, puis les recoller côté serveur via la logique de *merge*
  (`tock_web_connector_merge_stream_response`).

### 2. `sendStreamedToken(content: String)` — canal dédié (RAG streaming)

```kotlin
override fun sendStreamedToken(content: String) {
    if (!streamedResponse || content.isEmpty()) return
    val ctx = context ?: return
    val data = webMapper.writeValueAsString(mapOf("content" to content))
    ctx.vertx().runOnContext {
        val response = ctx.response()
        if (!response.ended()) {
            response.sendSseEvent("token", data)
        }
    }
}
```

- Entrée : un **fragment de texte brut** (`String`).
- Sortie : une frame SSE `event: token` minimale (`{"content": "..."}`).
- Part **hors-bande**, sans passer par le pipeline d'actions du moteur.

| | `sendStreamedResponse` | `sendStreamedToken` |
|---|---|---|
| Entrée | `Action` complète | fragment `String` |
| Frame SSE | `event: message` (JSON métier Tock) | `event: token` (`{content}`) |
| Déclencheur | moteur (`handleWebConnectorCallback`) | handler RAG (`onToken`) |
| Passe par le pipeline d'actions ? | **oui** (merge, persistance) | **non** |
| Nombre d'actions domaine | **N** (une par fragment) | **1** (la réponse finale) |

---

## Pourquoi un canal dédié ? L'avantage

**Une phrase découpée en 50 tokens n'est pas 50 messages — c'est 1 message.**

Le mécanisme natif oblige à matérialiser chaque fragment comme une vraie `Action`.
Ça remonte un détail purement cosmétique (le découpage en tokens) jusqu'au cœur du
moteur et pollue le modèle métier. Le canal `token` sépare proprement les deux
responsabilités :

1. **Domaine propre** — le moteur ne connaît qu'**une** action : le
   `SendSentenceWithFootnotes` final, complet, avec son texte, ses footnotes, sa trace
   d'observabilité. C'est l'unique action persistée dans le dialogue.

2. **Pas de merge artificiel** — on ne casse pas la réponse pour la recoller ensuite.
   Le texte progressif passe par `token`, le texte canonique arrive une seule fois.
   La logique `tock_web_connector_merge_stream_response` devient inutile pour la RAG.

3. **Découplage transport / métier** — le streaming est un choix d'affichage du client
   (opt-in via `streamedResponse=true`). Un client qui ne stream pas reçoit exactement
   la même réponse bufferisée qu'avant. Zéro impact sur le domaine.

4. **Format client trivial** — le front consomme du texte pur (`{content}`) à afficher
   au fil de l'eau, sans désérialiser ni merger des actions Tock.

### Flux de bout en bout

```
LLM (question_answering_llm)                               [Python]
  │ tokens
  ▼
/rag/stream → SSE : event "token" (×N) … puis event "answer"
  │
  ▼
RAGServiceImpl.ragStream → listener.onToken / onAnswer     [orchestrator-client]
  │
  ▼
RAGAnswerHandler.ragStream :                               [engine]
  • onToken(delta) → callback.sendStreamedToken(delta)   ← hors-bande, affichage
  • onAnswer(resp) → [retour de ragStream avec RAGResult]
  → handleProactiveAnswer : bus.send(SendSentenceWithFootnotes)  ← 1 action domaine
  │
  ▼
Connecteur web/rest :                                      [connecteurs]
  • event: token (×N)        ← progressif, non persisté
  • event: message / answer  ← réponse canonique finale
```

Le client affiche les `token` au fil de l'eau, puis **remplace** l'affichage par le
contenu de la frame finale (qui porte le texte complet + footnotes). Les tokens sont
éphémères ; la réponse finale est la source de vérité.

---

## Les inconvénients de garder ce deuxième canal

Il faut être honnête : ce choix a un coût. Aucun n'est bloquant, mais il faut les
assumer.

1. **Deux formats SSE à maintenir** — le client doit gérer `event: token` **et**
   `event: message`/`answer`. C'est une surface d'API plus large qu'un flux homogène.

2. **Deux chemins de code** — `sendStreamedResponse` (natif) et `sendStreamedToken`
   (RAG) coexistent dans `WebConnectorCallback` / `RestConnectorCallback`. Un
   développeur doit comprendre lequel s'applique et pourquoi.

3. **Cohérence token ↔ réponse finale non garantie par construction** — rien n'empêche
   mécaniquement que la concaténation des `token` diffère du texte de l'`answer` finale
   (guardrail post-stream qui réécrit/bloque, troncature…). C'est **justement** pourquoi
   la frame finale est la source de vérité et doit remplacer l'affichage progressif —
   mais c'est une convention, pas une contrainte du type.

4. **Threading** — les tokens arrivent sur un thread étranger (le thread SSE background
   de l'orchestrateur). Il faut re-marshaller chaque écriture sur le contexte Vert.x
   (`runOnContext`) pour préserver l'ordre et forcer le flush immédiat. C'est fait, mais
   c'est une subtilité à ne pas casser.

5. **Pas de persistance des fragments** — assumé et voulu ici, mais à garder en tête :
   si un jour on veut rejouer/auditer le flux token par token, l'info n'existe pas (seule
   la réponse finale est stockée).

6. **Les tokens ne portent que du texte** — pas de debug, pas de footnotes, pas de
   metadata sur les fragments. C'est voulu (le canal `token` est un canal de texte pur).
   Ce qui est structuré arrive sur la frame finale — mais attention, ce n'est **pas** une
   limitation du canal : c'est une dépendance de données. Voir la section suivante.

---

## Ce qui est streamable, ce qui ne l'est pas (dépendances de données)

On entend parfois : « pourquoi ne pas streamer aussi les footnotes / le debug /
l'observabilité ? ». La réponse dépend de **quand la donnée existe** dans la chaîne RAG.
Le flux Python (`execute_rag_chain_stream`) a trois étapes :

```
1. rag_inputs.ainvoke()   → retrieval + condensation   (AVANT le streaming)
2. answer_chain.astream() → tokens du champ "answer"    (PENDANT le streaming)
3. build_rag_response()   → footnotes / obs / debug      (APRÈS le streaming)
```

### Catégorie A — intrinsèquement terminal (ne *peut pas* être streamé)

- **Footnotes** — `build_footnotes()` filtre les documents sur
  `llm_answer.context_usage[].used_in_response`. Or `context_usage` est le **dernier**
  champ du JSON `LLMAnswer` (ordre : `status`, `answer`, …, `context_usage`). Le
  `JsonOutputParser` émet `answer` en premier et `context_usage` tout à la fin : on ne
  sait quelles sources sont « réellement utilisées » qu'une fois **toute** la réponse
  générée. Les footnotes définitives sont, par construction du prompt, l'ultime
  information produite.
- **Observability (trace Langfuse)** — `get_observability_info()` lit la trace **après**
  exécution ; l'URL/ID n'existe qu'une fois le run tracé. Post-exécution par nature.
- **`duration` / `step_durations`** — connues seulement à la fin.
- **`status`, `redirection_intent`, `score`, `display_answer`** — champs du même JSON
  `LLMAnswer`, fiables uniquement une fois le JSON complet.

### Catégorie B — disponible tôt (*pourrait* être streamé en avance, non fait aujourd'hui)

- **Documents récupérés (sources candidates)** — ils existent dès l'étape 1 (`prepared`),
  **avant** le premier token. On pourrait émettre un event `sources` **avant** les tokens
  (le sous-ensemble définitif « used » restant, lui, terminal — catégorie A).
- **Prompts de debug** (`rag_prompt`, `chat_prompt`, condensing) — capturés par le
  callback dès l'étape 1, donc streamables tôt.

### Catégorie C — le texte

- Déjà streamé : c'est précisément le rôle du canal `token`.

### Conclusion

Le fait que le structuré arrive sur la frame finale n'est donc **pas** une limitation du
canal `token`. C'est un mélange de :

- données **intrinsèquement terminales** (A) — les streamer n'a pas de sens, l'info
  n'existe pas encore ;
- données **front-loadables** (B) — techniquement anticipables (ex. un event `sources`
  au retrieval), simplement pas (encore) implémentées.

Le canal `token` stream déjà tout ce qui est streamable au fil de l'eau (le texte) ; le
reste attend la frame finale par dépendance de données, pas par contrainte de transport.

---

## Verdict

Le canal `token` est le bon choix : il garde le **modèle de domaine propre** (1 réponse =
1 action) tout en offrant l'affichage progressif. Le coût réel est la **coexistence de
deux mécanismes de transport** — acceptable tant que la frontière reste claire :

- `token` = transport/affichage, éphémère, hors-bande ;
- `SendSentenceWithFootnotes` final = domaine, persisté, source de vérité.

La seule règle stricte côté client : **la frame finale fait foi** et remplace l'affichage
construit à partir des tokens.
