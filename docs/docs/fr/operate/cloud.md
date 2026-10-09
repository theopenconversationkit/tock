---
title: Cloud & Haute disponibilité
---

# Cloud & Haute disponibilité

Cette page présente les aspects liés à l'utilisation de services _Cloud_ (privés ou publiques) pour déployer 
et héberger plateformes et bots Tock.

En effet, nous avons l'expérience d'utilisations de Tock en production sur des hébergements classiques _on-premise_ 
et _bare metal_, mais aussi sur des _Clouds privés_ comme [OpenStack](https://www.openstack.org/) ou _Clouds publiques_ 
comme [AWS](https://aws.amazon.com/).

## Haute disponibilité

Cette section fournit des conseils et des retours d'expérience sur les configurations 
_haute disponibilité_ (ou _HA - High Availability_) de bots et plateformes Tock.

### Redondance et résilience

Une seule instance de `tock/build_worker` doit exister.

Il est recommandé d'utiliser une seule instance de `tock/bot_admin` et `tock/kotlin_compiler`.
 
Pour les autres composants, en particulier le composant bot (non fourni) mais également `tock/bot_api`, `tock/nlp_api`,
`tock/duckling` et `tock/gen-ai-orchestrator-server`, il est recommandé de déployer plusieurs instances pour assurer
une meilleure disponibilité voire de meilleures performances.

Les sessions de _Tock Studio_ sont conservées en mémoire : avec plusieurs instances de `tock/bot_admin`, activez les
sessions persistantes (_sticky sessions_) sur le répartiteur de charge.

La disponibilité du RAG dépend aussi de la base vectorielle et des fournisseurs de LLM et d'embeddings :
voir leur documentation pour leurs propres options de haute disponibilité.

### Performance

Comme indiqué dans la section [installation](installation.md), le premier paramètre à surveiller est 
la mémoire disponible.

A forte charge - nous avons expérimenté plus de 80 req/s sur nos propres bots - 
le facteur limitant devient la base de données MongoDB, qu'il faut alors redimensionner en conséquence
quand le besoin s'en fait sentir.

Avec le RAG, le temps de réponse dépend surtout du fournisseur de LLM : ajustez
`tock_gen_ai_orchestrator_client_request_timeout_ms` et les délais de l'orchestrateur en conséquence
(voir [Configuration](configuration.md#orchestrateur-gen-ai)).

## Kubernetes

Le [chart Helm](kubernetes.md) déploie Tock sur Kubernetes. Quelle que soit la méthode
de déploiement, appliquez les recommandations ci-dessus :

* `tock/build_worker` : 1 réplica, avec la stratégie de mise à jour `Recreate` pour que deux instances ne tournent jamais en même temps
* `tock/bot_admin` et `tock/kotlin_compiler` : 1 réplica (ou des _sticky sessions_ pour `tock/bot_admin`)
* `tock/bot_api`, `tock/nlp_api`, `tock/duckling`, `tock/gen-ai-orchestrator-server` : plusieurs réplicas
* Sondes : `/health/readiness` et `/health/liveness` pour les composants JVM, `/health-check` et `/liveness-check`
  pour l'orchestrateur Gen AI (voir [Supervision](supervision.md#lignes-de-vie-healthchecks))
* Mémoire : fixez la limite mémoire du conteneur au-dessus du tas maximal de la JVM (`-Xmx` dans `JAVA_ARGS`,
  voir [Installation](installation.md#memoire-jvm-docker))
