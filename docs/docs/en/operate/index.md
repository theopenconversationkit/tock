---
title: Operate
---

# Operating a Tock platform

This section is for the teams that install, configure and run a Tock platform.

## Install

* [Architecture](architecture.md): the components of a platform (_Tock Studio_, NLU, _Bot API_, Gen AI orchestrator...)
  and their dependencies (MongoDB, vector store, LLM providers).
* [Installation](installation.md): deploying the platform with Docker or without Docker, MongoDB, sizing.
* [Kubernetes](kubernetes.md): deploying the platform with the Helm chart.
* [Cloud & High Availability](cloud.md): managed services, sizing and resilience in production.

## Configure and secure

* [Configuration reference](configuration.md): the configuration properties of every component.
* [Security](security.md): roles of the _Tock Studio_ users, data protection and secrets.
* [_Tock Studio_ authentication](authentication.md): users defined by properties, OAuth2 (Keycloak, GitHub...) or CAS.

## Run

* [Supervision](supervision.md): health checks and logs.
* [Upgrading Tock](upgrade.md): moving the platform and the bots to a new version.
* [Troubleshooting](troubleshooting.md): common problems and their causes.

To try Tock on your machine first, see [Deploy a platform with Docker](../getting-started/run-platform.md).
