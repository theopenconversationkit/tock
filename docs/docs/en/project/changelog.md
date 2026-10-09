---
title: Changelog
---

# Changelog

This page lists the main changes of each Tock release.
The complete list of changes (pull requests, contributors) is available in the
[GitHub releases](https://github.com/theopenconversationkit/tock/releases).

Tock versions follow the `YY.M.patch` scheme: `26.3.x` is the release line started in March 2026.
Artifacts are published on [Maven Central](https://central.sonatype.com/namespace/ai.tock) under the `ai.tock` group.

## 26.9.0 (2026-09-29)

* Java 21 is now the minimum version
* New `tock-bom` artifact, to align the versions of the Tock dependencies (see [Kotlin bot](../develop/kotlin-bot.md))
* Gen AI:
    * [AWS Bedrock](../gen-ai/providers/llm-embedding.md#aws-bedrock) support (LLM, embeddings, guardrails, reranking),
      see [RAG on AWS](../getting-started/rag-aws.md)
    * [Vector store inspection](../gen-ai/vector-store-inspection.md) tool
    * Indexing tool fixes, SQL schema for the PGVector hybrid search
* _Tock Studio_: bot [dashboard](../studio/dashboard.md), trimmed RAG form values
* Google Chat: feedback buttons
* WhatsApp: users identified by their Business-Scoped User ID (BSUID)

See the [release notes](https://github.com/theopenconversationkit/tock/releases/tag/tock-26.9.0).

## 26.3.4 (2026-09-01)

* _Tock Studio_:
    * Upgrade to Angular 21
    * Configurable environment banner
    * CSV import / export of datasets
    * FAQ import
* Gen AI: custom source labels in RAG answers, waiting message parameter
* Web connector: routing context properties, frontend locale update check
* Bot engine: new user data redaction mechanism

See the [release notes](https://github.com/theopenconversationkit/tock/releases/tag/tock-26.3.4).

## 26.3.3 (2026-07-03)

* Gen AI:
    * Multi-query retriever for RAG
    * Business glossary, covered topics and excluded topics in the RAG prompt context
* Bot engine: `transientContext` argument for `pushNotification`
* WhatsApp: WhatsApp Cloud API webhook model fixes

See the [release notes](https://github.com/theopenconversationkit/tock/releases/tag/tock-26.3.3).

## 26.3.2 (2026-05-22)

* _Tock Studio_:
    * Datasets: create an evaluation from a dataset, delete runs, import / export, RAG debug view
    * Dialogs: search by RAG response status
    * Namespace labels
    * "New sentence" section UX improvements in _Language Understanding_
* Gen AI:
    * LLM reasoning effort setting
    * Fault-tolerant document compressor
    * Langfuse trace name for the Playground
* Bot engine: coroutine-based checked `pushNotification`
* Security: externalized Vert.x session timeout, logout fix for `PropertyBasedAuthProvider`

See the [release notes](https://github.com/theopenconversationkit/tock/releases/tag/tock-26.3.2).

## 26.3.1 (2026-03-27)

* Fix: _Tock Studio_ not reachable with Vert.x 5.0.8

See the [release notes](https://github.com/theopenconversationkit/tock/releases/tag/tock-26.3.1).

## 26.3.0 (2026-03-24)

* Gen AI:
    * Structured LLM response and RAG metrics
    * Evaluation samples (API and _Tock Studio_ screens) and datasets
    * Chunk metadata and title fallback for RAG sources
* Google Chat: Google Workspace Add-on support
* Dependency upgrades

See the [release notes](https://github.com/theopenconversationkit/tock/releases/tag/tock-26.3.0).

## Previous releases

See the [GitHub releases](https://github.com/theopenconversationkit/tock/releases).
