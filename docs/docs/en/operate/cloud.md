---
title: Cloud & High Availability
description: "Deploy and host Tock platforms and bots on private or public clouds, with high availability."
---

# Cloud & High Availability

This page presents the aspects related to the use of _Cloud_ services (private or public) to deploy
and host Tock platforms and bots.

Indeed, we have experience of using Tock in production on classic _on-premise_ and _bare metal_ hosting, but also on _private Clouds_ like [OpenStack](https://www.openstack.org/) or _public Clouds_
like [AWS](https://aws.amazon.com/).

## High Availability

This section provides advice and feedback on the
_high availability_ (or _HA - High Availability_) configurations of Tock bots and platforms.

### Redundancy and resilience

A single instance of `tock/build_worker` must exist.

It is recommended to use a single instance of `tock/bot_admin` and `tock/kotlin_compiler`.

For other components, especially the bot component (not provided) but also `tock/bot_api`, `tock/nlp_api`,
`tock/duckling` and `tock/gen-ai-orchestrator-server`, it is recommended to deploy multiple instances to ensure
better availability or even better performance.

The sessions of _Tock Studio_ are kept in memory: with several `tock/bot_admin` instances, enable sticky sessions
on the load balancer.

The availability of the RAG also depends on the vector store and on the LLM and embedding providers:
see their documentation for their own high availability options.

### Performance

As indicated in the [installation](installation.md) section, the first parameter to monitor is
available memory.

At high load - we have experienced more than 80 req/s on our own bots -
the limiting factor becomes the MongoDB database, which must then be resized accordingly
when the need arises.

With the RAG, the response time mostly depends on the LLM provider: adjust
`tock_gen_ai_orchestrator_client_request_timeout_ms` and the timeouts of the orchestrator accordingly
(see [Configuration](configuration.md#gen-ai-orchestrator)).

## Kubernetes

The [Helm chart](kubernetes.md) deploys Tock on Kubernetes. Whatever the deployment
method, apply the recommendations above:

* `tock/build_worker`: 1 replica, with the `Recreate` update strategy so that two instances never run at the same time
* `tock/bot_admin` and `tock/kotlin_compiler`: 1 replica (or sticky sessions for `tock/bot_admin`)
* `tock/bot_api`, `tock/nlp_api`, `tock/duckling`, `tock/gen-ai-orchestrator-server`: several replicas
* Probes: `/health/readiness` and `/health/liveness` for the JVM components, `/health-check` and `/liveness-check`
  for the Gen AI orchestrator (see [Supervision](supervision.md#healthchecks))
* Memory: set the container memory limit above the maximum heap of the JVM (`-Xmx` in `JAVA_ARGS`,
  see [Installation](installation.md#jvm-docker-memory))
