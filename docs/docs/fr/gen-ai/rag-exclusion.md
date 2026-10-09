---
title: Sentences Rag exclusions
---

# Le menu _Sentences Rag exclusions_

Les exclusions RAG retirent des sujets du périmètre des réponses générées. Une phrase exclue du RAG est qualifiée
avec l'intention `tock:ragexcluded` : une fois que le modèle NLU a appris suffisamment de phrases de ce type, les phrases
utilisateur similaires sont reconnues comme exclues, et le bot y répond sans appeler le LLM
(voir [Comment le bot répond](how-it-works.md#exclure-des-sujets-du-rag)).

C'est plus sûr qu'une consigne dans le [prompt](rag-prompt.md), car la décision est prise avant l'appel au LLM.

![RAG Exclusions - Entraînement](../img/gen-ai/gen-ai-rag-excluded-1.png "Écran de configuration des sujets à exclure de l'IA")

## Exclure des phrases

1. Allez dans le menu _Language Understanding_ > _Inbox sentences_
   (le rôle **_nlpUser_** suffit, voir [sécurité](../operate/security.md#roles))
2. Sélectionnez la phrase que vous souhaitez exclure
3. Cliquez sur _Exclude from Rag handling_ (ou sélectionnez plusieurs phrases et utilisez l'action groupée _Rag excluded_)

Comme pour toute intention, qualifiez plusieurs phrases variées pour chaque sujet exclu : une seule phrase suffit rarement
pour que le modèle reconnaisse les autres façons de poser la même question. Le modèle est reconstruit automatiquement
après la qualification (voir [Modèles conversationnels](../studio/build-model.md)). Vérifiez le résultat dans le menu [_Test_](../studio/test.md).

## Lister les phrases exclues

L'écran _Gen AI_ > _Sentences Rag exclusions_ liste toutes les phrases exclues du RAG
(le rôle **_admin_** est nécessaire, comme pour les autres menus _Gen AI_).

![RAG Exclusions](../img/gen-ai/gen-ai-rag-excluded-2.png "Écran des sujets exclus de l'IA")

## Réponse aux phrases exclues

Par défaut, le bot répond _"Sorry, I can't answer your question (Topic not covered)"_. Cette réponse est un libellé
du bot : traduisez-la ou modifiez-la dans _Stories & Answers_ > _Answers_ (voir [Internationalisation](../studio/i18n.md)).
Un bot développé en Kotlin peut aussi remplacer toute la story par la `ragExcludedStory` de sa `BotDefinition`.
