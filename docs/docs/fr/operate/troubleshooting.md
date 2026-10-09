---
title: Dépannage
---

# Dépannage

Cette page liste des problèmes fréquents et leurs causes habituelles. Commencez par consulter les logs et les
[lignes de vie](supervision.md#lignes-de-vie-healthchecks) des composants.

## Les composants ne démarrent pas

**Erreurs de connexion à MongoDB.** Tock nécessite un _replica set_ MongoDB, même avec un seul nœud :
`tock_mongo_url` doit contenir le paramètre `replicaSet` et le _replica set_ doit être initialisé
(voir [Installation](installation.md#architecture-replica-set)).

**Erreur `no 'tock_encrypt_pass' set`.** Hors environnement de dev (`tock_env` différent de `dev`), la propriété
`tock_encrypt_pass` est obligatoire dès que des données sont chiffrées (voir [Sécurité](security.md#chiffrement-applicatif)).

## Je ne peux pas me connecter à _Tock Studio_

**Les identifiants sont refusés.** Par défaut, les identifiants sont `admin@app.com` / `password`. Si `tock_users`
et `tock_passwords` sont définis, ils remplacent cet utilisateur par défaut (voir [Authentification _Tock Studio_](authentication.md#implementation-par-proprietes)).

**La connexion réussit, mais la session est aussitôt perdue.** Hors environnement de dev, le cookie de session
n'est envoyé qu'en HTTPS. Servez _Tock Studio_ en HTTPS, ou définissez `tock_https_env=false` si le TLS est géré par un
proxy qui transmet des requêtes HTTP. Avec plusieurs instances de `bot_admin`, les sessions ne sont pas partagées : utilisez
une seule instance, ou des sessions persistantes (_sticky sessions_) sur le répartiteur de charge.

## Le bot ne répond pas

**Dans _Test_ > _Test_, le bot répond par une erreur.** Le connecteur de test appelle le bot à l'_Application base url_
de sa configuration, dans _Settings_ > _Configurations_ : cette URL doit être joignable depuis le service `bot_admin`
(par exemple `http://bot_api:8080` avec les descripteurs `tock-docker`, voir [Configuration](../studio/configuration.md)).

**En mode _Bot API_, rien ne se passe.** Consultez les logs de votre bot : la clé d'API doit être celle de la
configuration, et le bot doit joindre le service `bot_api` (mode _WebSocket_) ou être joignable par lui
(mode _WebHook_, voir [Bot API](../develop/bot-api.md)).

**Le bot répond avec une autre story.** La phrase n'est pas reconnue comme l'intention attendue :
qualifiez-la dans _Language Understanding_ > _Inbox sentences_ (voir [Compréhension du langage](../studio/nlu.md)),
et vérifiez que l'intention est l'intention principale de la story, dans _Stories & Answers_ > _All stories_.

**Les nouvelles phrases ne sont pas prises en compte.** Le modèle est reconstruit par le service `build_worker` peu de temps
après la validation d'une phrase : vérifiez que ce service fonctionne, et consultez les constructions dans
_Model Quality_ > _Model Builds_.

## Le RAG ne répond pas

**Le bot répond par une erreur technique.** L'orchestrateur Gen AI n'est pas joignable : vérifiez
`tock_gen_ai_orchestrator_server_url` dans les services `bot_api` et `bot_admin`, et la ligne de vie de l'orchestrateur
(`/health-check`). Depuis Tock 26.3.0, le prompt _Question answering_ doit aussi produire un objet JSON
(voir [Mettre à jour Tock](upgrade.md#2630)).

**Les réponses ne citent jamais de document.** Vérifiez qu'une session d'indexation est sélectionnée dans les
[réglages du RAG](../gen-ai/rag.md#session-dindexation), et que la base vectorielle configurée dans _Tock Studio_ ou par les
variables d'environnement est celle où les documents ont été indexés : la base vectorielle par défaut n'est pas la même dans
_Tock Studio_ et les bots, et dans l'orchestrateur (voir [Vector DB settings](../gen-ai/vector-store.md)).
Le [diagnostic de recherche](../gen-ai/vector-store-inspection.md#diagnostic-de-recherche) montre les documents trouvés
pour une question.

## La construction du modèle échoue

**`OutOfMemoryError` dans le `build_worker`.** La mémoire nécessaire augmente avec le nombre de phrases, d'intentions et
d'entités : voir les [recommandations de mémoire](installation.md#memoire-jvm-docker).
