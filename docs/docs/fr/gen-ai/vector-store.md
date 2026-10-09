---
title: Vector DB settings
description: "Configurer la base de données vectorielle à laquelle le RAG d'un bot Tock est connecté."
---

# Le menu _Vector DB settings_

## Configuration

Le menu _Gen AI_ > _Vector DB settings_ permet de configurer la base vectorielle à laquelle le RAG du bot sera connecté.

Dans l'IA, les bases vectorielles sont utilisées pour représenter des données sous forme de vecteurs, facilitant des opérations comme la similarité sémantique ou la classification. 
Elles sont notamment utilisées dans les modèles d'apprentissage automatique pour traiter et analyser des textes, des images ou d'autres types de données complexes.

> Pour accéder à cette page il faut bénéficier du rôle **_admin_**.
> <br />(plus de détails sur les rôles dans [sécurité](../operate/security.md#roles)).

Un écran de configuration permet à Tock de se connecter à une base vectorielle :

![Vector Store](../img/gen-ai/gen-ai-settings-vector-store.png "Écran de configuration des bases vectorielles")

## Utilisation

- Voici la [liste des fournisseurs de base vectorielle](providers/vector-store.md) qui sont pris en compte par Tock.
- Veuillez vous référer à la documentation de chaque outil pour comprendre comment l'utiliser.
- Si aucune configuration n'a été fournie dans _Tock Studio_, la configuration par défaut, définie par des variables d'environnement, est utilisée.

Dans le backend de _Tock Studio_ (`tock/bot_admin`) et les bots, `tock_gen_ai_orchestrator_vector_store_provider` définit le type de la base vectorielle par défaut
(`PGVector` par défaut). Cela permet à Tock, lors d'un appel RAG, de construire les bons paramètres de recherche pour cette base.

Dans l'orchestrateur Gen AI :

| Variable | Défaut | Description |
|----------|--------|-------------|
| `tock_gen_ai_orchestrator_vector_store_provider` | `OpenSearch` | `OpenSearch` ou `PGVector` |
| `tock_gen_ai_orchestrator_vector_store_host` | `localhost` | Hôte de la base vectorielle |
| `tock_gen_ai_orchestrator_vector_store_port` | `9200` | Port (par exemple `5432` pour PGVector) |
| `tock_gen_ai_orchestrator_vector_store_user` | `admin` | Utilisateur |
| `tock_gen_ai_orchestrator_vector_store_pwd` | `admin` | Mot de passe |
| `tock_gen_ai_orchestrator_vector_store_database` | | Nom de la base (PGVector uniquement) |
| `tock_gen_ai_orchestrator_vector_store_secret_manager_provider` | | `AWS_SECRETS_MANAGER` ou `GCP_SECRET_MANAGER`, pour lire les identifiants dans un gestionnaire de secrets |
| `tock_gen_ai_orchestrator_vector_store_credentials_secret_name` | | Nom du secret contenant les identifiants |
| `tock_gen_ai_orchestrator_vector_store_timeout` | `4` | Délai maximal d'une requête, en secondes |

> Le fournisseur par défaut n'est pas le même dans _Tock Studio_ et les bots (`PGVector`) et dans l'orchestrateur (`OpenSearch`) :
> donnez la même valeur à la variable des deux côtés.
