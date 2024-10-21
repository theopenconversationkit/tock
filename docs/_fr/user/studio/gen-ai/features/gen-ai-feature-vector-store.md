---
title: Le menu Gen AI - Vector Store Settings
---

# Le menu _Gen AI - Vector Store Settings_

## Configuration

Le menu _Gen AI - Vector Store Settings_ permet de configurer la base vectorielle à laquelle le RAG du bot sera connecté.

Dans l'IA, les bases vectorielles sont utilisées pour représenter des données sous forme de vecteurs, facilitant des opérations comme la similarité sémantique ou la classification. 
Elles sont notamment utilisées dans les modèles d'apprentissage automatique pour traiter et analyser des textes, des images ou d'autres types de données complexes.

> Pour accéder à cette page il faut bénéficier du rôle **_botUser_**.
> <br />( plus de détails sur les rôles dans [securité](../../../../../admin/securite#rôles) ).


## Configuration
Pour permettre à Tock de se connecter à une base vectorielle, un écran de configuration a été mis en place :

![Vector Store](../../../../../img/gen-ai/gen-ai-feature-observability.png "Ecran de configuration des bases vectorielles")

## Utilisation

- Voici la [liste des fournisseurs de base vectorielle](../../providers/gen-ai-provider-vector-store) qui sont pris en compte par Tock.
- Veuillez vous référer à la documentation de chaque outil pour comprendre comment l'utiliser.
- Si aucune configuration n'a été fournie via le Tock studio, la configuration par défaut (spécifiée via les variable d'environnement) sera privilégiée :
  - Dans Bot Admin, la variable `tock_gen_ai_orchestrator_vector_store` spécifie la nature de la base vectorielle par défaut. Cela permet à Tock, lors d'un appel RAG, de construire correctement les paramètres de recherche dans cette base.
  - Dans l'Orchestrateur, voici la liste des variables d'environnements à préciser :
    - `tock_gen_ai_orchestrator_vector_store_provider` Ex: PGVector
    - `tock_gen_ai_orchestrator_vector_store_host` Ex: localhost
    - `tock_gen_ai_orchestrator_vector_store_port` Ex: 5432
    - `tock_gen_ai_orchestrator_vector_store_user` Ex: postgres
    - `tock_gen_ai_orchestrator_vector_store_pwd` Ex: postgres
    - `tock_gen_ai_orchestrator_vector_store_database` Ex: postgres
    - `tock_gen_ai_orchestrator_vector_store_secret_manager_provider` Ex: GCP
    - `tock_gen_ai_orchestrator_vector_store_credentials_secret_name` Ex: my-secret-name
