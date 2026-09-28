---
title: Observability settings
---

# Le menu _Observability settings_

- L'observabilité des modèles de langage (LLM Observability) aide à surveiller, d'analyser et de comprendre le comportement des modèles de langage à grande échelle.
- Cela inclut la collecte de données sur leurs performances, la détection d'anomalies et la compréhension des erreurs qu'ils peuvent produire. 
- L'objectif est de garantir que ces modèles fonctionnent de manière fiable, transparente, en fournissant des informations qui permettent d'améliorer leur performance et de corriger les problèmes potentiels.
- Plus précisément, nous pourrons :
    - Voir les différents enchainements d'appels de LLM avec le prompt d'entrée et de sortie
    - Analyser les portions de documents contextuels utilisés
    - Suivre les informations et les métriques sur les coûts, le nombre de jetons consommés, la latence, etc.


> Pour accéder à cette page il faut bénéficier du rôle **_admin_**.
> <br />( plus de détails sur les rôles dans [securité](../operate/security.md#roles) ).

## Configuration
Pour permettre à Tock de se connecter à un outil d'observabilité, un écran de configuration a été mis en place : 

![LLM Observability](../img/gen-ai/gen-ai-feature-observability.png "Ecran de configuration de l'outil d'observation de l'IA")

## Configuration d'URL Publique

- Le champ **Public URL** permet de spécifier une URL accessible depuis l'extérieur pour les outils d'observabilité comme Langfuse.
- Cette URL sera utilisée dans l'interface frontend pour rediriger les utilisateurs vers les traces d'observabilité, remplaçant l'URL interne qui pourrait ne pas être accessible publiquement.

## Utilisation

- Voici la [liste des fournisseurs d'observabilité des LLM](providers/observability.md) qui sont pris en compte par Tock.
- Veuillez vous référer à la documentation de chaque outil pour comprendre comment l'utiliser.
