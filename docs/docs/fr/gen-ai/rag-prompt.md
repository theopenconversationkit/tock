---
title: Prompt RAG
description: "Écrire et ajuster le prompt de réponse d'un bot RAG Tock, étape par étape."
---

# Le prompt RAG

> **Public :** développeurs, intégrateurs et concepteurs de prompts.
> La structure du prompt et sa sortie JSON sont détaillées dans la [référence du prompt RAG](rag-prompt-reference.md).

## En bref : rédiger le prompt de votre bot

1. Partez du modèle le plus proche de votre cas d'usage : bot client grand public, conseiller métier interne ou
   bot développeurs / exploitation (voir [Typologie des cas d'usage](#typologie-des-cas-dusage)).
2. Conservez la **section 1** (règles système) telle quelle, et remplissez la **section 2** (règles métier) en suivant
   le [guide de configuration](#configurer-un-nouveau-bot-guide-de-la-section-2).
3. Gérez les thèmes couverts, les thèmes exclus et le lexique métier dans le menu
   [_Rag prompt context_](rag-prompt-context.md) plutôt que dans le texte du prompt : ils sont injectés à l'exécution.
4. Collez le prompt dans la partie _Question answering_ des [_Rag settings_](rag.md#reponse-question-answering), après
   l'avoir essayé dans le [playground](playground.md).
5. Vérifiez l'effet sur un [dataset](answers-quality.md#datasets) de questions représentatives
   (voir [Améliorer les réponses](improve.md)).

Les champs `status`, `topic` et `suggested_topics` de la réponse alimentent les indicateurs RAG du dashboard et de
_Metrics_, et `redirection_intent` fait basculer la conversation sur une story
(voir [Comment le bot répond](how-it-works.md#rediriger-du-rag-vers-une-story)).

## Présentation

[Tock](https://doc.tock.ai) est une plateforme open source de création d'agents conversationnels, utilisée dans de nombreux secteurs et organisations. Le framework de prompt RAG décrit dans ce document définit une **architecture de prompt structurée et réutilisable** pour les chatbots à base de LLM fonctionnant dans la chaîne RAG (Retrieval-Augmented Generation) de Tock.

Sa principale caractéristique est que le LLM doit renvoyer un **objet JSON strictement structuré** plutôt qu'une réponse en texte libre. Ce contrat de sortie permet aux systèmes en aval de :

- router les réponses automatiquement en fonction d'un champ `status`,
- déclencher des actions (par exemple un transfert vers un humain) via un champ `redirection_intent`,
- auditer les documents retrouvés réellement utilisés via un tableau `context_usage`,
- suivre la confiance et la classification par thème sans traitement NLP supplémentaire.

Tous les prompts partagent le même squelette. Seules les **règles métier (section 2)** varient d'un déploiement à l'autre.

## Configurer un nouveau bot — guide de la section 2

Pour déployer un nouveau bot avec ce framework, seule la **section 2** doit être rédigée. Les autres sections sont réutilisées telles quelles (avec, si besoin, un assouplissement des contraintes de la section 1).

### Étape 1 — Définir l'identité du bot (2.1)

```markdown
- **Name:** <Bot name>
- **Role:** <What does the bot do and for whom>
- **Domain:** <The knowledge domain it operates in>
- **Target Audience:** <Who will interact with it>
- **Response language:** {{locale}}
```

**Conseils :**

- Soyez précis sur le public : il influence le ton et le niveau de technicité par défaut.
- La description du domaine est utilisée par le LLM pour la validation du domaine (section 1.1).

### Étape 2 — Définir le périmètre (2.2)

Listez les thèmes que le bot **couvre** et ceux qu'il **ne couvre pas**.

```markdown
**Covered Topics:**

- Small talk
- <Topic A>
- <Topic B>

**Excluded Topics:**

- Personal/Private Matters
- Legal advice
- <Any domain-specific exclusion>
```

**Conseils :**

- Incluez toujours `Small talk` dans les thèmes couverts pour permettre les salutations et la conversation courante.
- Soyez explicite dans les thèmes exclus : l'ambiguïté rend le comportement `out_of_scope` incohérent.
- Les noms de thèmes deviennent les valeurs autorisées pour le champ `topic` de la sortie JSON.

### Étape 3 — Fixer les attentes sur les réponses (2.3)

```markdown
- **Required Depth Level:** <Concise / Detailed / Balanced>
- **Level of Technicality:** <Low / Moderate / High>
- **Assumptions Allowed:** <What can the bot assume about the user's knowledge>
```

### Étape 4 — Définir le style et le ton (2.4)

```markdown
- **Tone:** <Formal / Neutral / Friendly / Empathetic / ...>
- **Formatting:** <Markdown / Plain text / Bullet points / Code blocks / ...>
- **Vocabulary Constraints:** <Jargon allowed? Which terminology to use?>
```

**Conseils :**

- Pour les bots destinés aux clients finaux : un langage simple et accessible.
- Pour les bots techniques internes : du Markdown avec des blocs de code est recommandé.
- Précisez si le bot doit tutoyer ou vouvoyer en français, ou les marqueurs de formalité équivalents dans les autres langues.

### Étape 5 — Ajouter les contraintes propres au domaine (2.5)

```markdown
- **Regulatory Constraints:** <e.g. no financial/legal advice>
- **Compliance Rules:** <e.g. data privacy requirements>
- **Forbidden Statements:** <e.g. no speculation on unreleased products>
- **Mandatory Mentions:** <e.g. always cite product name with month + year>
- **Smart suggestions:** <e.g. suggest related topics when unable to answer>
- **Absolute URLs only:** <never generate relative links>
```

### Étape 6 — Ajouter des instructions spécifiques si besoin (2.6)

Cette sous-section facultative accueille toute logique qui n'entre pas dans les champs standards. Exemples courants :

| Cas d'usage                    | Exemple d'instruction                                                                      |
| ------------------------------ | ------------------------------------------------------------------------------------------ |
| Désambiguïsation de produits   | Demander à l'utilisateur de préciser le mois et l'année quand plusieurs variantes d'un produit existent |
| Logique de transfert vers un humain | Définir quand proposer un transfert et comment le déclencher                          |
| Contacts de repli              | Fournir une adresse email de repli si le contexte ne permet pas de répondre                |
| Plusieurs environnements       | Demander une précision quand plusieurs environnements sont possibles                      |

### Étape 7 — Revoir les contraintes de la section 1

Décidez si les contraintes par défaut de la section 1 conviennent à ce bot :

| Contrainte                   | Par défaut | Quand l'assouplir                                            |
| ---------------------------- | ---------- | ------------------------------------------------------------ |
| Réponses RAG uniquement      | Stricte    | Le bot traite des sujets dev/IT non couverts par les documents |
| Anti-hallucination           | Stricte    | À garder stricte pour les domaines métier sensibles           |
| Protection contre l'injection | Stricte   | Ne jamais assouplir                                           |
| Validation du domaine        | Stricte    | Ne jamais assouplir                                           |

En cas d'assouplissement de la section 1.2 (politique RAG), ajoutez une note explicite, par exemple :

> _Pour les sujets hors du domaine métier principal (questions générales de développement, génération de code), le LLM peut s'appuyer sur ses connaissances propres quand aucun contexte pertinent n'est retrouvé._

## Typologie des cas d'usage

Tock étant une plateforme open source utilisée dans de nombreux secteurs et organisations, le framework de prompt RAG doit s'adapter à des contextes de déploiement très différents. Trois cas d'usage types ont été identifiés, chacun avec ses propres priorités de configuration.

### Type A — Bot client grand public

**Exemple de prompt :** [modèle de prompt de type A](https://github.com/theopenconversationkit/tock/blob/master/docs/docs/en/gen-ai/prompt-examples/prompt-example-type-a-end-user-customer-bot.md)

**Profil :** un bot exposé directement au grand public ou aux clients finaux d'une entreprise. Les utilisateurs n'ont pas d'expertise particulière et attendent des réponses simples, rassurantes et accessibles.

**Contextes de déploiement typiques :** banque de détail, e-commerce, assurance, services publics, support client télécom.

**Caractéristiques principales :**

| Dimension                    | Recommandation                                                                                 |
| ---------------------------- | ---------------------------------------------------------------------------------------------- |
| **Ton**                      | Chaleureux, empathique, poli, bienveillant                                                     |
| **Technicité**               | Faible — éviter tout jargon                                                                    |
| **Rigueur RAG**              | Élevée — les réponses doivent s'appuyer strictement sur la documentation                       |
| **Transfert vers un humain** | Fortement recommandé — proposer un conseiller quand la confiance est faible ou la question personnelle |
| **Mise en forme**            | Texte simple, phrases courtes, pas de syntaxe Markdown                                         |
| **Périmètre**                | Étroit et bien défini — le refus hors périmètre doit être clair sans être frustrant            |
| **Contraintes réglementaires** | Élevées — pas de conseil juridique ou financier, aucune supposition sur la situation personnelle |

**Instructions spécifiques à envisager (section 2.6) :**

- Définir des déclencheurs explicites de transfert (par exemple quand le contexte est insuffisant, ou quand la situation de l'utilisateur est trop individuelle pour être traitée de façon générique).
- Distinguer un conseiller en direct (joignable via le chat) du conseiller personnel de l'utilisateur (qui ne peut pas être contacté par ce canal).
- Utiliser `redirection_intent` pour déclencher un transfert fluide côté front, sans rompre la conversation.

### Type B — Bot d'aide aux collaborateurs métier

**Exemple de prompt :** [modèle de prompt de type B](https://github.com/theopenconversationkit/tock/blob/master/docs/docs/en/gen-ai/prompt-examples/prompt-example-type-b-internal-business-advisor-bot.md)

**Profil :** un bot qui assiste les collaborateurs ou les experts métier d'une organisation — conseillers, équipes commerciales, analystes, équipes opérationnelles. Les utilisateurs connaissent le domaine, mais ont besoin d'un accès rapide et fiable à des informations structurées sur les produits ou les processus.

**Contextes de déploiement typiques :** support commercial, bases de connaissance produits, accompagnement conformité, procédures internes, assistance aux conseillers terrain.

**Caractéristiques principales :**

| Dimension                    | Recommandation                                                                                  |
| ---------------------------- | ----------------------------------------------------------------------------------------------- |
| **Ton**                      | Formel, professionnel, direct                                                                   |
| **Technicité**               | Moyenne — la terminologie du domaine est acceptée et attendue                                   |
| **Rigueur RAG**              | Élevée — les réponses doivent venir de la documentation officielle ; pas d'improvisation sur les sujets métier |
| **Transfert vers un humain** | Facultatif — un contact de repli (email, canal interne) suffit souvent                          |
| **Mise en forme**            | Structurée : termes clés en gras, listes à puces, sections claires                              |
| **Périmètre**                | Centré sur des gammes de produits, des processus ou des domaines de connaissance précis         |
| **Contraintes réglementaires** | Moyennes à élevées selon le domaine (produits financiers, conformité, etc.)                  |

**Instructions spécifiques à envisager (section 2.6) :**

- Ajouter une logique de désambiguïsation des produits ou entités quand plusieurs éléments proches existent (par exemple des produits différenciés par date, version ou région).
- Définir des règles de mention obligatoire (par exemple toujours citer le nom complet du produit, avec sa version ou sa date).
- Fournir un point de contact de repli quand la documentation ne couvre pas la question.

### Type C — Bot pour les développeurs et les opérations techniques

**Exemple de prompt :** [modèle de prompt de type C](https://github.com/theopenconversationkit/tock/blob/master/docs/docs/en/gen-ai/prompt-examples/prompt-example-type-c-developer-ops-bot.md)

**Profil :** un bot qui assiste des ingénieurs, des équipes DevOps ou des opérateurs techniques. Les utilisateurs sont très techniques et attendent des réponses précises et directement exploitables — code, commandes, patterns d'architecture, pistes de débogage.

**Contextes de déploiement typiques :** documentation d'infrastructure, support de stack applicative, portails développeurs internes, runbooks OPS, accompagnement CI/CD.

**Caractéristiques principales :**

| Dimension                    | Recommandation                                                                                                                                                                  |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Ton**                      | Neutre, concis, entre pairs                                                                                                                                                     |
| **Technicité**               | Élevée — le jargon technique est attendu et approprié                                                                                                                           |
| **Rigueur RAG**              | **Assouplie** — le LLM peut utiliser ses connaissances propres sur les sujets techniques génériques (patterns de code, outils standards, architectures courantes) quand le contexte retrouvé est insuffisant |
| **Transfert vers un humain** | Rarement nécessaire — rediriger vers les canaux internes (responsables d'équipe, référents OPS) hors périmètre                                                                  |
| **Mise en forme**            | Markdown obligatoire : blocs de code, `code` en ligne, termes clés en gras                                                                                                      |
| **Périmètre**                | Large périmètre technique avec des exclusions explicites (par exemple pas de RH, pas de juridique)                                                                              |
| **Contraintes réglementaires** | Faibles pour les sujets génériques ; potentiellement plus fortes pour les procédures sensibles en matière de sécurité                                                          |

**Instructions spécifiques à envisager (section 2.6) :**

- Indiquer explicitement que le LLM peut s'appuyer sur ses connaissances propres pour les sujets non couverts par la documentation (patterns de code, utilisation d'outils, concepts informatiques génériques).
- Demander une précision quand une question peut concerner plusieurs environnements, stacks ou configurations.
- Appliquer une logique de suggestion : quand il ne peut pas répondre, le bot suggère des concepts ou outils voisins présents dans le contexte.
- Ne fournir que des URL absolues — jamais de liens relatifs.

### Synthèse comparative

| Dimension                        | Type A — Grand public        | Type B — Collaborateurs métier | Type C — Développeurs / OPS       |
| -------------------------------- | ---------------------------- | ------------------------------ | --------------------------------- |
| **Public principal**             | Grand public / clients       | Experts métier / collaborateurs | Ingénieurs / opérateurs techniques |
| **Ton**                          | Chaleureux, empathique       | Formel, professionnel          | Neutre, concis                    |
| **Technicité**                   | Faible                       | Moyenne                        | Élevée                            |
| **Rigueur RAG**                  | Élevée                       | Élevée                         | Assouplie pour les sujets génériques |
| **Transfert vers un humain**     | ✅ Recommandé                | ⚠️ Facultatif (contact de repli) | ❌ Rarement nécessaire           |
| **Mise en forme**                | Texte simple                 | Texte structuré                | Markdown + blocs de code          |
| **Largeur du périmètre**         | Étroite                      | Ciblée                         | Technique et large                |
| **Point d'attention de la section 2.6** | Logique de transfert  | Logique de désambiguïsation    | Assouplissement des connaissances propres |

> Ces trois types sont des profils de référence, pas des catégories rigides. Un déploiement réel peut combiner des caractéristiques de plusieurs types selon les besoins de l'organisation et le profil du public visé.
