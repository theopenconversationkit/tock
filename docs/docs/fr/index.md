---
title: Accueil
---

# Bienvenue sur Tock : une plateforme conversationnelle ouverte, avec ou sans IA générative

**Tock** (*The Open Conversation Kit*) est une plateforme complète et ouverte pour construire des assistants
conversationnels : répondre à partir de vos documents avec le LLM de votre choix (RAG), dérouler des parcours
maîtrisés écrits par vos équipes, ou combiner les deux.

Contrairement à la plupart des solutions conversationnelles, Tock ne dépend pas d'API tierces, bien qu'il soit possible d'en intégrer.
L'utilisateur choisit les composants qu'il embarque, y compris des LLM locaux, et peut ainsi conserver la maîtrise de
ses modèles et données conversationnelles.

> Tock est utilisé en production depuis 2016 par SNCF (assistant OUI.sncf, aujourd'hui SNCF Connect)
> (Web/mobile, réseaux sociaux, enceintes connectées) et [de plus en plus d'organisations](project/showcase.md) 
> (ENEDIS, Linagora, AlloCovid...).

L'ensemble du code source est disponible sur [GitHub](https://github.com/theopenconversationkit/tock) sous 
[licence Apache 2](https://github.com/theopenconversationkit/tock/blob/master/LICENSE). 

## Avec LLM, sans LLM, ou les deux

| | Avec LLM | Sans LLM | Mixte |
|---|---|---|---|
| **Principe** | Le [RAG](gen-ai/rag.md) répond à partir de vos documents | Le modèle NLU détecte l'intention, des [stories](studio/stories-and-answers.md) et des [FAQ](studio/faq.md) répondent | Le modèle NLU oriente chaque phrase vers une story ou vers le RAG |
| **Réponses** | Générées, avec leurs sources | Écrites et validées par vos équipes | Écrites pour les parcours sensibles, générées pour le reste |
| **Adapté à** | Bases documentaires larges, questions ouvertes | Parcours transactionnels, contextes réglementés, bots embarqués sans Internet, pas de coût d'inférence | La plupart des assistants en production |
| **Pour commencer** | [Tutoriel RAG](getting-started/rag-tutorial.md) | [Premier bot avec Tock Studio](getting-started/first-bot-studio.md) | [Comment le bot répond](gen-ai/how-it-works.md) |

L'IA générative est une brique optionnelle : l'orchestrateur Gen AI n'est déployé que si vous l'utilisez,
et un bot existant peut activer le RAG depuis _Tock Studio_.

## Aperçu

Des [guides](getting-started/first-bot-studio.md), [supports](project/resources.md) et une [video de démonstration](https://www.youtube.com/watch?v=UsKkpYL7Hto) 
(20 minutes, en Anglais) sont également disponibles :

<a href="https://www.youtube.com/watch?v=UsKkpYL7Hto"
target="tock_osxp">

![logo rest-api](img/tockosxp2021.png "rest api")
</a>

## Fonctionnalités

* IA générative (voir [Gen AI](gen-ai/index.md)) :
    * Réponses _RAG_ (Retrieval-Augmented Generation) à partir de vos documents, avec leurs sources,
      avec les bases vectorielles [PGVector](https://github.com/pgvector/pgvector) ou [OpenSearch](https://opensearch.org/)
    * Fournisseurs de LLM et d'embeddings : [OpenAI](https://openai.com/), [Azure OpenAI](https://azure.microsoft.com/products/ai-services/openai-service),
      [Ollama](https://ollama.com/) pour les modèles locaux...
    * Maîtrise des réponses : [stories et FAQ combinées au RAG](gen-ai/how-it-works.md), thèmes couverts et exclus,
      prompt de réponse structuré
    * [Amélioration continue](gen-ai/improve.md) : diagnostic de recherche, playground, observabilité des LLM avec
      [Langfuse](https://langfuse.com/), jeux de données et évaluation des réponses
* Interfaces _Tock Studio_ :
    * Configuration des fonctionnalités Gen AI, de la base de connaissances et des prompts
    * Construction de parcours conversationnels, de FAQ et d'arbres de décision sans code
    * Support de l'internationalisation (_i18n_) pour les bots multilingues
    * Suivi des conversations, tendances / parcours utilisateurs (_Analytics_) et qualité des réponses
* Plateforme _NLU_ complète _<sup>([Natural Language Understanding](https://en.wikipedia.org/wiki/Natural-language_understanding) 
ou [TAL](https://fr.wikipedia.org/wiki/Traitement_automatique_du_langage_naturel) en français)</sup>_,
qui oriente chaque phrase vers un parcours ou vers le RAG :
    * Utilisant des briques open-source comme [OpenNLP](https://opennlp.apache.org/), [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/),
[Duckling](https://github.com/facebook/duckling), [Rasa](https://rasa.com/),
ou des modèles hébergés sur [AWS SageMaker](https://aws.amazon.com/sagemaker/)
    * Déployable seule si besoin pour des usages comme l'[_Internet des objets_](https://fr.wikipedia.org/wiki/Internet_des_objets)
* Assistants autonomes ou intégrés à des sites Web, applications mobiles, réseaux sociaux, enceintes connectées etc. 
sans dépendre d'un canal particulier
* Frameworks pour développer des parcours complexes et intégrer des services tiers : <br/> _DSLs_ en 
[Kotlin](https://kotlinlang.org/), [Javascript/Nodejs](https://nodejs.org/), [Python](https://www.python.org/) 
et _API_ tous langages (voir [_Bot API_](develop/bot-api.md))
* Nombreux connecteurs texte et voix : [Messenger](https://www.messenger.com/), [WhatsApp](https://www.whatsapp.com/), 
[Teams](https://www.microsoft.com/microsoft-teams/), [Slack](https://slack.com/), [Google Chat](https://workspace.google.com/products/chat/), [Mattermost](https://mattermost.com/), [iAdvize](https://www.iadvize.com/),
[Alcmeon](https://www.alcmeon.com/), clients compatibles OpenAI,
un connecteur Web avec des kits [React](https://reactjs.org) et [Flutter](https://flutter.dev/)... (voir [canaux](channels/index.md))
* Installation _cloud_ ou _on-premise_, avec ou sans [Docker](https://www.docker.com/), sur [Kubernetes](operate/installation.md#installation-sur-kubernetes),
même _"embarqué"_ sans Internet 

![Réponse RAG avec ses sources, testée dans Tock Studio](img/gen-ai/gen-ai-rag-test.png "Réponse RAG avec ses sources, testée dans Tock Studio")

## Technologies

L'ensemble de la plateforme peut fonctionner _conteneurisée_ (implémentation [Docker](https://www.docker.com/) fournie). 

La plateforme applicative par défaut est la [JVM](https://fr.wikipedia.org/wiki/Machine_virtuelle_Java). 
Le langage de référence est [Kotlin](https://kotlinlang.org/) mais d'autres langages de programmation peuvent être utilisés via les API mises à disposition.

Côté serveur, Tock utilise [Vert.x](http://vertx.io/) et [MongoDB](https://www.mongodb.com ) <sup>(alt. [DocumentDB](https://aws.amazon.com/fr/documentdb/))</sup>. 
Différentes briques _NLU_ peuvent être utilisées, mais Tock n'a pas de dépendance forte envers l'une d'elles.

Les interfaces graphiques _Tock Studio_ sont écrites avec [Angular](https://angular.dev/) en [Typescript](https://www.typescriptlang.org/).

L'orchestrateur Gen AI est un service [Python](https://www.python.org/) construit avec [FastAPI](https://fastapi.tiangolo.com/)
et [LangChain](https://www.langchain.com/), qui appelle les fournisseurs de LLM, d'embeddings et de bases vectorielles.

Des intégrations [React](https://reactjs.org) et [Flutter](https://flutter.dev/) sont fournies pour les interfaces Web et Mobile.

## Démarrer...

* [Tutoriels](getting-started/index.md), à commencer par le [tutoriel RAG](getting-started/rag-tutorial.md), et [plateforme de démonstration](https://demo.tock.ai/)
* [Gen AI](gen-ai/index.md) : RAG, base de connaissances, prompts, qualité des réponses
* Manuels [utilisateur](studio/index.md), [développeur](develop/index.md), [administrateur](operate/architecture.md)
* [Ressources (supports, video)](project/resources.md) et [exemples de code](develop/examples.md)

[NLU]: https://en.wikipedia.org/wiki/Natural-language_understanding "Natural Language Understanding"
*[NLU]: Natural Language Understanding