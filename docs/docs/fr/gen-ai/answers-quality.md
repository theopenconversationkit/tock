---
title: Qualité des réponses
---

# Le menu _Answers Quality_

Le menu _Answers Quality_ aide les équipes métier à mesurer et à améliorer la qualité des réponses du bot,
en particulier celles générées par le [RAG](rag.md). Il propose deux outils :

* les [_Évaluations_](#evaluations) : des personnes relisent et notent un échantillon de conversations réelles (ou de test) ;
* les [_Datasets_](#datasets) : une liste fixe de questions est rejouée sur le bot, pour comparer les exécutions
  après un changement de réglages, de prompt ou de documents.

> Pour accéder à ce menu, il faut bénéficier du rôle **_botUser_**
> (plus de détails sur les rôles dans [sécurité](../operate/security.md#roles)).

## Évaluations

Un _échantillon d'évaluation_ est un ensemble de dialogues dont les réponses du bot sont relues une par une.

### Créer un échantillon

![New evaluation sample](../img/studio/evaluation-new-sample.png "New evaluation sample")

Cliquez sur **NEW SAMPLE** et renseignez :

* le nom (_Sample name_) et une description facultative (_Sample description_),
* le nombre de dialogues à tirer au hasard (_Number of dialogues_),
* la période couverte, avec une date de début et une date de fin : seuls les dialogues créés pendant cette période sont retenus,
* _Allow test dialogues_ : si les dialogues créés depuis les tests de _Tock Studio_ peuvent être retenus.

Un échantillon peut aussi être [créé à partir d'une exécution de dataset](#creer-un-echantillon-a-partir-dune-execution).

La liste des échantillons indique pour chacun son type (aléatoire ou généré depuis un dataset), son état
(_In Progress_ ou _Validated_), son score positif, son nombre de dialogues et de réponses, et qui l'a créé et validé.

### Évaluer les réponses

Ouvrez un échantillon pour relire ses dialogues. Pour chaque réponse du bot :

* évaluez-la positivement (pouce levé),
* ou évaluez-la négativement (pouce baissé) en choisissant une raison :
    * question non ou mal comprise
    * réponse inexacte
    * réponse incomplète
    * sources / documents incomplets
    * sources / documents obsolètes
    * problème de lexique métier
    * mauvais format de réponse
    * hallucination
    * autre

La barre de progression indique le nombre de réponses évaluées, et les évaluations positives et négatives.
Le détail de l'échantillon donne la période couverte, le nombre de dialogues demandés et obtenus, le nombre total de
dialogues enregistrés pour le bot sur la période, et le taux de couverture qui en découle.

### Valider un échantillon

Une fois toutes les réponses évaluées, cliquez sur **Validate** et ajoutez un commentaire si besoin.
Un échantillon validé ne peut plus être modifié. Il peut être exporté en rapport PDF avec **Export**.

> Les dialogues sont soumis à la politique de rétention des données de la plateforme.
> Si certains dialogues d'un échantillon ont été purgés, un avertissement s'affiche et l'échantillon ne peut plus être validé.

## Datasets

Un _dataset_ est une liste nommée de questions, chacune avec une _vérité terrain_ facultative (la réponse attendue).
Exécuter un dataset envoie chaque question au bot et enregistre les réponses, pour pouvoir comparer deux exécutions
question par question : typiquement avant et après un changement des réglages RAG, du prompt ou des documents indexés.

### Créer un dataset

![Datasets](../img/studio/datasets-list.png "Datasets")

Cliquez sur **NEW DATASET**, puis saisissez un nom, une description facultative et les questions,
avec pour chacune une vérité terrain facultative.

Un dataset peut aussi être importé (bouton _Import a dataset_) :

* depuis un fichier JSON contenant un `name`, une `description` et un tableau de `questions`,
  chacune avec un champ `question` et un champ `groundTruth`,
* ou depuis un fichier CSV avec une colonne `question` et une colonne `groundTruth` facultative :
  téléchargez le _CSV template_ depuis la fenêtre d'import pour obtenir les colonnes attendues.
  Le nom et la description sont alors saisis dans la fenêtre.

![Edit dataset](../img/studio/dataset-edit.png "Edit dataset")

Depuis l'écran d'édition, **EXPORT DATASET** exporte le dataset complet en JSON, ou seulement ses questions en CSV.

> Les résultats des exécutions sont rapprochés par question. Reformuler légèrement une question ne pose pas de problème,
> mais en changer le sens fausse la comparaison : dans ce cas, supprimez la question et ajoutez-en une nouvelle.

### Exécuter un dataset

Cliquez sur _Run dataset_. Chaque question est envoyée au bot avec le [connecteur de test](../studio/test.md), comme si elle était
saisie dans le menu _Test_, avec le mode debug et le contenu des sources activés. Les questions sont traitées une par une,
en arrière-plan : la liste affiche la progression de l'exécution en cours, qui peut être annulée.

Exécuter un dataset nécessite une configuration de test pour le bot. Une seule exécution peut être active par dataset,
et un dataset ne peut pas être modifié ou supprimé pendant une exécution.

Pour chaque exécution, la liste indique ses dates, sa durée et son état, et compte les questions :

* _Completed_ et _Failed_ (échecs d'exécution ou erreurs techniques),
* _Intent_ : traitées par la détection d'intention,
* _Found in context_ : traitées par le RAG avec des documents pertinents,
* _Not found in context_ : traitées par le RAG sans document pertinent.

Une exécution qui se termine avec des questions en échec reste _terminée_ : les échecs sont comptés dans ses statistiques.

### Comparer des exécutions

Ouvrez une exécution pour afficher ses réponses. L'écran compare une _Run A_ et une _Run B_ (par défaut, la dernière exécution),
question par question : texte de la réponse, type de réponse (RAG ou intention) et sources. Les sources ajoutées,
modifiées ou supprimées entre les deux exécutions sont mises en évidence.

Les filtres _Show only diffs_ n'affichent que les questions dont l'état, les sources, le type de réponse ou la formulation ont changé.

Les liens _settings_ et _prompts_ affichent les réglages RAG et les prompts utilisés par chaque exécution, et les comparent.

### Créer un échantillon à partir d'une exécution

Depuis la liste des exécutions, _Create an evaluation sample based on the results of this run_ crée un
[échantillon d'évaluation](#evaluations) contenant les réponses de l'exécution, pour qu'elles soient notées par des personnes.

## Configuration

Les propriétés suivantes du backend de _Tock Studio_ (`tock/bot_admin`) configurent les datasets :

| Variable d'environnement                   | Défaut  | Description                                                    |
|--------------------------------------------|---------|----------------------------------------------------------------|
| `tock_datasets_max_questions`              | `200`   | Nombre maximum de questions par dataset                        |
| `tock_dataset_run_worker_poll_interval_ms` | `2000`  | Intervalle entre deux recherches d'exécutions à traiter (ms)   |
| `tock_dataset_run_worker_max_retries`      | `0`     | Nombre de nouvelles tentatives d'une question après une erreur technique |
