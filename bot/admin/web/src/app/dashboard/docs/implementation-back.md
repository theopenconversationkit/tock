# Dashboard — implémentation DERCBOT-2100

## Périmètre livré

Le dashboard utilise `DashboardRestService`. Les cinq commits du front ont été repris
sur master, qui contient déjà l’inspection vectorielle. Aucun service supplémentaire
ni activation de feature flag n’est nécessaire.

Les routes suivent les conventions du serveur admin (préfixe habituel du déploiement) :

| Route                                                | Lecture               | Écriture           |
| ---------------------------------------------------- | --------------------- | ------------------ |
| `/bots/{botId}/identity`                             | GET, botUser ou admin | PUT, admin         |
| `/bots/{botId}/contacts`                             | GET, botUser ou admin | PUT, admin         |
| `/bots/{botId}/index-sessions/{indexSessionId}/note` | GET, botUser ou admin | PUT, admin         |
| `/bots/{botId}/history`                              | GET, botUser ou admin | interne uniquement |

Le namespace et l’auteur viennent de la session authentifiée. L’application désignée
par l’URL doit appartenir au namespace courant. Un GET sans métadonnées renvoie une
identité vide, une liste de contacts vide ou une note vide, selon la ressource.

## Précisions par rapport au chantier initial

- **Stockage** : `bot_dashboard_metadata` regroupe identité et contacts dans un document
  par `{namespace, botId}`. C’est l’option d’entité associée autorisée par le §2 ; elle
  évite de modifier le modèle NLP et ses conversions. Identité et contacts sont mis à
  jour séparément. Les contacts sont une liste, sans collection par contact.
- **Identifiants de contacts** : le serveur attribue un `id` aux nouveaux contacts,
  indispensable à l’édition/suppression dans le front existant. Les identifiants reçus
  sont conservés ; une liste contenant des doublons est refusée. Le PUT remplace la
  liste entière ; en cas de modifications concurrentes, la dernière écriture l’emporte.
- **Historique** : `bot_history_event` stocke les huit types prévus. La création correspond
  à la création de l’application via le Studio. Les connecteurs REST techniques générés
  automatiquement ne créent pas de doublon. Les évaluations émettent lors du passage à
  `VALIDATED`, avec `positiveRate` sur 0–100 et `dialogCount` égal au nombre de dialogues.
- **Points d’écriture** : les cinq services de configuration sont instrumentés, ainsi
  que l’import de stories qui désactive directement le RAG. L’auteur est transmis depuis
  le contexte HTTP. Il n’y a ni backfill, ni observation des écritures directes en Mongo
  ou des créations par une autre API que le Studio.
- **Snapshots** : état fonctionnel complet de l’entité persistée, sans résolution des
  secrets via les DTO. Les champs techniques de premier niveau `_id`/`id`, `namespace`
  et `botId` sont exclus de la projection (le rattachement est porté par l’événement).
  Les propriétés `apiKey`, `password` et `secretKey` sont retirées récursivement avant
  stockage. Le premier événement d’un type conserve `previous = null` ; les suivants
  contiennent le `current` du dernier événement de ce type.
- **Changements réels** : l’état persisté avant sauvegarde permet également d’ignorer
  un premier enregistrement sans modification sur un bot déjà existant. Les changements
  portant uniquement sur les secrets sont ignorés, puisque les snapshots expurgés sont
  identiques. Aucun événement à diff vide n’est ajouté pour ces changements.
- **Pagination** : `before` est un curseur opaque encodant la date et l’ObjectId Mongo.
  Une date seule peut sauter des événements de même milliseconde. La réponse est
  `{events, hasMore, nextCursor}` ; le front renvoie `nextCursor` comme `before` lorsqu’on
  clique « Charger la suite ». `limit` vaut 50 par défaut et doit être compris entre 1
  et 200. Index de lecture `{namespace, botId, date, _id}` et index du dernier événement
  `{namespace, botId, type, date, _id}`.
- **Inspection vectorielle** : le front extrait `indexes` de l’enveloppe `{indexes: [...]}`
  réellement livrée sur master. Il lit `/capabilities` pour connaître le fournisseur
  effectif et éviter une fausse alerte sur OpenSearch, dont l’inspection n’est pas
  implémentée. Une erreur de lecture ne devient pas une alerte « session absente ».
- **Diffs complets** : la modale montre aussi `lexiconGroups` dans `business-rules`, ainsi
  que les propriétés `formatter` et `inputs` des prompts en plus du diff de `template`.
- **Purge** : les trois collections sont nettoyées par `BotAdminService.deleteApplication`.
  Une note reste conservée si seule la session disparaît du vector store. Pas de TTL sur
  l’historique tant que le bot existe.

## Garantie de l’historique

La sauvegarde métier précède l’écriture d’historique, comme prévu dans la doc.
Une erreur de journalisation est loguée sans secrets et ne transforme pas une sauvegarde
réussie en échec. Cette timeline est informative : ce n’est pas un audit transactionnel
exhaustif. Une panne entre les deux écritures peut laisser un événement absent ; des
sauvegardes concurrentes ne sont pas sérialisées entre instances. Les snapshots stockés
restent autonomes et lisibles. Aucun système de reprise ou de transaction distribuée
n’est ajouté dans ce chantier.

## Vérification manuelle

Lancer l’application avec le serveur admin, le front et les dépendances habituelles.
Pour les index, l’orchestrateur doit également inclure le code d’inspection de master.
Les collections et index Mongo sont créés automatiquement au premier accès ; aucun
script de migration des bots existants n’est requis.

1. Sur un bot existant, ouvrir `/dashboard` : identité, contacts et historique peuvent
   être vides. Les widgets de statistiques continuent à utiliser les endpoints existants.
2. Modifier l’identité, créer deux contacts, puis en éditer/supprimer un ; recharger la
   page et vérifier la persistance et l’indépendance de l’identité et des contacts.
3. Sur un bot RAG utilisant PGVector, vérifier la date et les volumes de l’index ; créer
   une note puis recharger. Une note peut aussi être lue/éditée via son endpoint après
   disparition de sa session du store.
4. Enregistrer la configuration RAG sans rien changer : aucun événement. Modifier une
   valeur : un événement `rag-settings`, auteur courant, état seul la première fois.
   Modifier une autre valeur : snapshot avant/après. Un changement de session après ce
   premier événement doit être mis en évidence comme changement de corpus.
5. Modifier un prompt, les sujets puis le lexique ; inspecter les différences. Tester
   également une modification de configuration vector store, compresseur et observabilité.
6. Importer une story avec l’option qui désactive le RAG : vérifier l’événement RAG.
7. Créer un bot, ajouter/modifier un connecteur, valider un échantillon d’évaluation :
   vérifier les événements correspondants et l’absence de doublon du connecteur technique.
8. Avec plus de 50 événements, charger la suite. Changer de bot pendant un chargement :
   les réponses de l’ancien contexte ne doivent pas remplacer celles du nouveau bot.
9. Avec un utilisateur botUser sans rôle admin, vérifier les lectures et le refus des PUT.
10. Supprimer un **bot de test** : ses métadonnées, notes et historique doivent disparaître.

Tests ciblés : `BotDashboardServiceTest`, `BotDashboardMongoDAOTest`,
`DashboardVerticleTest`, les tests existants de `RAGServiceTest` et les specs du dashboard
sur le contrat d’inspection, la pagination et les diffs.
