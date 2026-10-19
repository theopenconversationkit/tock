---
title: Exploiter
description: "Installer, configurer et exploiter une plateforme Tock : architecture, déploiement, sécurité et supervision."
---

# Exploiter une plateforme Tock

Cette section s'adresse aux équipes qui installent, configurent et font fonctionner une plateforme Tock.

## Installer

* [Architecture](architecture.md) : les composants d'une plateforme (_Tock Studio_, NLU, _Bot API_, orchestrateur Gen AI...)
  et leurs dépendances (MongoDB, base vectorielle, fournisseurs de LLM).
* [Installation](installation.md) : déployer la plateforme avec ou sans Docker, MongoDB, dimensionnement.
* [Kubernetes](kubernetes.md) : déployer la plateforme avec le chart Helm.
* [Cloud & Haute disponibilité](cloud.md) : services managés, dimensionnement et résilience en production.

## Configurer et sécuriser

* [Référence de configuration](configuration.md) : les propriétés de configuration de chaque composant.
* [Sécurité](security.md) : rôles des utilisateurs de _Tock Studio_, protection des données et secrets.
* [Authentification _Tock Studio_](authentication.md) : utilisateurs définis par propriétés, OAuth2 (Keycloak, GitHub...) ou CAS.

## Faire fonctionner

* [Supervision](supervision.md) : contrôles de santé et logs.
* [Mettre à jour Tock](upgrade.md) : passer la plateforme et les bots à une nouvelle version.
* [Dépannage](troubleshooting.md) : problèmes fréquents et leurs causes.

Pour d'abord essayer Tock sur votre machine, voir [Déployer une plateforme avec Docker](../getting-started/run-platform.md).
