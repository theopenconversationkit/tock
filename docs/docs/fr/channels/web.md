---
title: Web
---

# Connecteur Web

Le connecteur Web expose une API HTTP générique pour dialoguer avec un bot Tock. Il sert à intégrer les bots dans des
sites web et des applications mobiles, avec les kits [React](index.md#react) et [Vue](index.md#vue),
ou avec n'importe quel autre client.

* **Type de connecteur** : `web`
* **Sources et README** : [connector-web](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-web)
* **API** : [descripteur Swagger](../../api/web-connector.html)

## Configuration

Dans _Tock Studio_, ajoutez un connecteur _Web_ à la configuration du bot (_Settings > Configurations_), avec son chemin
relatif, par exemple `/web`.

| Champ | Description |
|-------|-------------|
| Web Security Mode | (facultatif) mode d'identification des utilisateurs, voir [Modes de sécurité](#modes-de-securite) (défaut : `DEFAULT`) |
| Public Path (if different from local REST Path) | (facultatif) chemin du connecteur vu par le navigateur, derrière un reverse proxy. Utilisé comme chemin des cookies de sécurité |

## Utilisation de l'API

Envoyer un message :

```shell
curl localhost:8080/web --data '{"query":"Bonjour","userId":"id"}'
```

La réponse contient les messages du bot : textes, boutons, cartes, carrousels, widgets...

```json
{
  "responses": [
    {
      "text": "Bonjour !",
      "buttons": [
        {
          "title": "Itinéraires",
          "payload": "search?_previous_intent=greetings"
        }
      ]
    }
  ]
}
```

Sélectionner un bouton en envoyant son payload :

```shell
curl localhost:8080/web --data '{"payload":"search?_previous_intent=greetings","userId":"id"}'
```

## Server-sent events (SSE) et streaming

Avec les [Server-sent events](https://developer.mozilla.org/fr/docs/Web/API/Server-sent_events)
(`tock_web_sse=true`), les réponses du bot sont aussi envoyées sous forme d'événements sur une connexion persistante :
les utilisateurs voient les premiers messages immédiatement, et le bot peut envoyer des [messages push](#messages-push).
Le `tock-react-kit` utilise le flux SSE quand il est disponible.

Les messages qui ne peuvent pas être délivrés (utilisateur hors ligne, ou connecté à une autre instance) sont stockés dans
MongoDB et délivrés à la reconnexion de l'utilisateur : le SSE fonctionne avec des déploiements multi-instances.

Avec `tock_web_direct_sse=true`, un message peut aussi être envoyé sur la route `<chemin>/sse/direct` : les réponses
sont alors streamées directement sur la même requête.

Quand le bot streame ses réponses (par exemple les réponses RAG générées par un LLM), les morceaux streamés sont
regroupés dans la réponse finale (`tock_web_connector_merge_stream_response`, `true` par défaut).

### Messages push

Quand le SSE est activé, le bot peut envoyer des messages push avec la méthode `notify`. Contrairement aux applications
de messagerie, rien ne garantit que l'utilisateur reçoive le message : il a pu fermer son navigateur.
S'il rouvre la page, il le reçoit malgré tout grâce à la file de messages.

## Modes de sécurité

Le mode de sécurité du connecteur se choisit dans sa configuration :

| Mode                | Description |
|---------------------|-------------|
| `DEFAULT`           | `COOKIES` si `tock_web_cookie_auth` vaut `true` (ou `basic`), `COOKIES_ENCRYPTED` s'il vaut `encrypted`, `PASSTHROUGH` sinon |
| `PASSTHROUGH`       | Aucune authentification : l'identifiant de l'utilisateur est fourni par le client |
| `COOKIES`           | L'identifiant de l'utilisateur est stocké dans un cookie `tock_user_id` sécurisé et HTTP-only, généré par le serveur |
| `COOKIES_ENCRYPTED` | Le cookie `__Http-tock_user_token` stocke un identifiant et une date d'expiration chiffrés (AES-GCM, clé dérivée de `tock_encrypt_pass`) |
| `JWT`               | Les JSON Web Tokens sont validés par votre propre implémentation de `WebSecurityHandler`, enregistrée avec le tag `JWT` via un `BotAdditionalModulesService` |

Les navigateurs n'envoient pas les cookies d'un domaine à l'autre : avec les modes à base de cookies, un reverse proxy
est nécessaire quand le backend du bot et le site web sont sur des domaines différents.

## Autres fonctionnalités

* **Markdown** : avec `tock_web_enable_markdown=true`, le texte des messages est converti de Markdown en HTML.
* **Widgets personnalisés** : implémentez `WebWidget` pour envoyer des données personnalisées au client.
  Avec le SSE, enregistrez la classe du widget dans KMongo (`KMongoConfiguration.bsonMapper.subtypeResolver.registerSubtypes(...)`).
* **En-têtes supplémentaires** : `tock_web_connector_extra_headers` liste les en-têtes HTTP supplémentaires autorisés
  depuis le client (par exemple des en-têtes d'authentification). Avec `tock_web_connector_use_extra_header_as_metadata_request=true`,
  leurs valeurs sont disponibles comme métadonnées dans le bot.

## Propriétés de configuration

| Propriété                                  | Défaut  | Description |
|--------------------------------------------|---------|-------------|
| `tock_web_cors_pattern`                    | toute origine | Regex des origines CORS acceptées |
| `tock_web_sse`                             | `false` | Active les server-sent events |
| `tock_web_direct_sse`                      | `false` | Active la route SSE directe |
| `tock_web_sse_keepalive_delay`             | `10`    | Secondes entre deux pings SSE |
| `tock_web_sse_message_queue_ttl_days`      | `-1`    | Jours avant suppression des messages non délivrés (MongoDB 7.1+ ; négatif : jamais) |
| `tock_web_sse_message_queue_max_count`     | `50000` | Nombre maximum de messages en file |
| `tock_web_sse_message_queue_max_size_kb`   | 2 × nombre max | Taille maximum de la file de messages (Ko) |
| `tock_web_cookie_auth`                     | aucun   | `true` ou `encrypted` : mode de sécurité utilisé par `DEFAULT` |
| `tock_web_cookie_auth_max_age`             | session | `Max-Age` du cookie, en secondes |
| `tock_web_cookie_auth_path`                | aucun   | `Path` du cookie, pour le partager entre connecteurs |
| `tock_web_enable_markdown`                 | `false` | Convertit les messages Markdown en HTML |
| `tock_web_connector_extra_headers`         | aucun   | En-têtes HTTP supplémentaires autorisés depuis le client |
| `tock_web_connector_use_extra_header_as_metadata_request` | `false` | Transmet les en-têtes supplémentaires comme métadonnées |
| `tock_web_connector_merge_stream_response` | `true`  | Regroupe les réponses streamées dans la réponse |
