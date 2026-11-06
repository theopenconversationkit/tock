# DERCBOT-2177 — Gestion de la base vectorielle : arbitrages fonctionnels

Décisions prises et précisions issues de la recette du 6 novembre 2026.

## Objectif et périmètre

Permettre aux administrateurs de gérer les chunks et les index depuis l’écran
« Exploration de la base vectorielle » : supprimer un chunk, consulter les métadonnées,
compléter le modèle d’embedding manquant et supprimer un index non courant.
Ces nouvelles actions concernent **PGVector uniquement**, dans le périmètre du bot sélectionné.

L’**index sélectionné** est celui que l’utilisateur consulte dans l’explorateur.
L’**index courant** est celui renseigné dans la configuration RAG du bot, même si le RAG est désactivé.
Changer la sélection dans l’explorateur ne change ni la configuration du bot ni la destination des publications de la knowledge base.

## Supprimer un chunk : quelle conséquence sur la source ?

| Situation | Décision retenue | Pourquoi ? |
| --- | --- | --- |
| Chunk d’une fiche de la knowledge base dans l’index courant | Dépublier la fiche, puis retirer son contenu via la synchronisation existante. La fiche reste disponible en brouillon. | La fiche publiée est la source du contenu actif. Supprimer seulement sa projection laisserait une fiche publiée incohérente avec l’index, susceptible d’être réindexée. |
| Chunk de cette même fiche dans un ancien index | Supprimer uniquement cette copie dans l’index sélectionné ; conserver le statut de la fiche. | Nettoyer une ancienne ingestion ne doit pas retirer la connaissance utilisée actuellement par le bot. |
| Chunk d’un document importé, dans n’importe quel index | Supprimer uniquement le chunk ciblé dans l’index sélectionné ; conserver le document source et les autres chunks. | L’explorateur gère le contenu indexé. Il ne définit pas de règle d’exclusion permanente pour les outils d’ingestion. |

Exemple : une fiche « Horaires » possède une copie dans l’index courant et une autre dans un ancien index.
Supprimer l’ancienne copie conserve la fiche publiée et sa copie courante.
Supprimer la copie courante dépublie la fiche ; la synchronisation retire ensuite son contenu de l’index courant.

La suppression d’un chunk est autorisée dans l’index courant : seule la suppression de l’index entier est interdite.
Chaque action demande une confirmation adaptée à sa conséquence : suppression locale ou dépublication.
Une fiche conservée en brouillon peut être republiée vers un index configuré et exploitable.
Une nouvelle ingestion d’un document importé peut recréer un chunk supprimé : aucune liste d’exclusion n’est créée.

## Dernier chunk et index vide

**Décision : autoriser la suppression du dernier chunk**, en annonçant dans la confirmation que l’index deviendra vide.
La collection est conservée : vider un index et supprimer un index sont deux actions distinctes.

La recette a confirmé une limite du parcours existant : un ancien index vidé, sans les métadonnées permettant
de le reconnaître comme un index créé par Tock, est considéré comme manquant ou inexploitable par la knowledge base.
Le renseigner dans les paramètres RAG ne permet donc pas de le réalimenter par le parcours normal de publication.
Le simple ajout du modèle d’embedding ne change pas ce constat.

**La règle existante de configuration RAG est conservée.** Pour cet ancien index, l’interface permet de le supprimer
et de créer un nouvel index depuis la knowledge base ; réutiliser exactement la collection vide demanderait
un outil d’ingestion capable de la cibler. Les index reconnus comme créés par Tock peuvent avoir un traitement
différent à vide ; cette limite ne doit donc pas être généralisée à tous les index.
Un index qui reste configuré conserve dans tous les cas sa protection contre la suppression complète.

L’action « Re-créer un index » préexistait à ce ticket : elle crée un nouvel index avec les fiches publiées,
peut faire pointer le bot dessus et conserve l’état activé ou désactivé du RAG. Elle ne reprend pas les extraits
documentaires importés présents dans l’ancien index. Ce comportement n’a pas été ajouté par DERCBOT-2177.

## Métadonnées et modèle d’embedding

| Question | Décision retenue | Pourquoi ? |
| --- | --- | --- |
| Que montrer pour un ancien index ? | Afficher toutes les métadonnées disponibles, même partielles ; signaler simplement leur absence si nécessaire. | Permettre l’inspection et la reprise des anciennes ingestions. |
| Peut-on modifier librement les métadonnées ? | Consultation de l’ensemble ; seule la propriété du modèle d’embedding manquant est éditable. Les autres propriétés sont conservées. | Le ticket porte sur la reprise du modèle historique, pas sur un éditeur générique de métadonnées. |
| Quand peut-on ajouter le modèle ? | Seulement s’il est absent, nul ou renseigné avec une chaîne vide ou des espaces. Un modèle déjà défini ne peut pas être écrasé. | Protéger l’information décrivant les vecteurs déjà stockés. |
| Quel modèle proposer ? | L’utilisateur renseigne le modèle historique ayant réellement produit les vecteurs. Un bouton permet de reprendre le modèle actuel du bot, puis l’utilisateur confirme. | Le modèle configuré aujourd’hui ne prouve pas quel modèle a servi à une ancienne ingestion. |
| Que faire si le modèle historique diffère du modèle du bot sur l’index courant ? | Enregistrer l’information et afficher un avertissement d’incompatibilité. | Conserver une description fidèle de l’index, sans masquer un problème de configuration. Certaines opérations de publication peuvent alors être bloquées par les contrôles existants. |
| Cet ajout migre-t-il les vecteurs ? | Aucun recalcul des vecteurs et aucune modification automatique de la configuration du bot. | Décrire les données existantes et migrer une ingestion sont des opérations différentes. |

## Supprimer un index entier

- L’index courant est toujours protégé, même s’il est vide ou si le RAG est désactivé.
- Les autres index sont supprimables avec tous leurs chunks, après confirmation explicite.
- La suppression d’un ancien index ne dépublie aucune fiche de la knowledge base.
- Un index encore alimenté par une tâche de création de la knowledge base ne peut pas être supprimé pendant cette tâche.

Le statut d’index courant vient de la configuration du bot, et non des métadonnées portées par les chunks :
la protection reste donc effective après suppression du dernier chunk.

## Retours UI/UX intégrés pendant la recette

| Observation | Choix retenu |
| --- | --- |
| Le bouton de suppression occupait une ligne isolée. | L’aligner à droite des informations d’ingestion. Pour l’index courant, le griser sans ajouter de texte permanent expliquant la protection. |
| Une phrase rappelait en permanence que le modèle ne peut pas être modifié. | Retirer cette phrase. Afficher le formulaire lorsque l’ajout est possible, sinon afficher simplement les métadonnées. |
| Après suppression d’un chunk, le sélecteur affichait son texte d’attente alors que le bon index restait consulté. | Conserver le nom de l’index sélectionné après rechargement, y compris lorsqu’il devient vide. Si l’index entier disparaît, sélectionner un index restant ; si aucun ne reste, vider l’affichage. |
| Le filtre annonçait une recherche par identifiant sans la réaliser. | Chercher dans le titre, l’identifiant du document affiché sous le titre et le contenu. Accepter une saisie partielle, sans distinction de majuscules, et retirer les espaces en début et fin de saisie. |

L’identifiant recherché est celui du **document**, partagé par ses chunks ; ce n’est pas l’identifiant de l’index
ni l’identifiant physique d’une ligne de stockage. Une correspondance conserve l’affichage de tous les chunks du document
dans l’index sélectionné. Les filtres d’anomalie continuent à s’appliquer.

## Cohérence et retour utilisateur

- Les règles sont contrôlées côté serveur, avec vérification du bot, de l’index courant et de l’origine réelle du chunk.
- Une confirmation devenue obsolète est refusée si elle changerait le sens de l’action, par exemple si un ancien index est devenu courant entre-temps.
- Les opérations concurrentes ne doivent permettre ni l’écrasement d’un modèle déjà renseigné ni la suppression de l’index courant.
- Une suppression directe cible une seule ligne physique dans la collection sélectionnée, même si des identifiants logiques sont dupliqués.
- Les listes, compteurs, métadonnées et pages sont actualisés après les opérations.
- Pour une dépublication, l’interface distingue la fiche passée en brouillon du retrait effectif de son contenu : elle suit la tâche de synchronisation et signale l’attente ou l’échec.
- « Publié » exprime le statut de la fiche ; « Indexation en attente » indique que sa projection dans l’index n’est pas encore à jour. Un échec réseau ne doit pas être présenté comme une indexation réussie.
