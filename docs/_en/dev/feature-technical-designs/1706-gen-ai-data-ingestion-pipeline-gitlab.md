---
title: Gen AI Data Ingestion pipeline (gitlab) design
---

# Gen AI Data Ingestion pipeline (gitlab) design #1706

- Proposal PR: [https://github.com/theopenconversationkit/tock/pull/1713](https://github.com/theopenconversationkit/tock/pull/1713)
- Github Issue for this feature: [#1706](https://github.com/theopenconversationkit/tock/issues/1706)


## Introduction

Data Ingestion is a complex task and ingested documents needs to be refreshed / renewed continuously. For now this task can be performed using our basic python tooling available here [tock-llm-indexing-tools](https://github.com/theopenconversationkit/tock/blob/tock-24.3.4/gen-ai/orchestrator-server/src/main/python/tock-llm-indexing-tools/README.md).

This is done manually and we are going to automate it a be more and also include testing features based on Langfuse datasets. 

Our approach will be based on Gitlab pipelines, this solution is simple and will let us schedule data ingestion or even trigger them using Gitlab's API. We will also be able to keep track of each ingestion jobs using gitlab and each job states.


## Overall pipeline

TODO make a clean workflow diagram using Mermaid.

[![Workflow - Data Ingestion Pipeline (excalidraw)](../../../img/feat-design-1706-data_ingestion_gitlab_pipeline_workflow.excalidraw.png)](../../../img/feat-design-1706-data_ingestion_gitlab_pipeline_workflow.excalidraw.png){:target="_blank"}


### Gitlab repositories organisation

TODO: for each project we will assume that we will need some kind of scripting to fetch data, clean it, organize it, keep important metadata ..
we need to think about how we will organize this repositories, pipeline dependencies ? session ID / naming convention if we have multiple repositories ...


## Architecture design

This design will illustrate 2 cloud based architecture for AWS and GCP (using kubernetes).

### AWS deployment

[![Architecture AWS - Data Ingestion Pipeline (excalidraw)](../../../img/feat-design-1706-data_ingestion_gitlab_architecture_aws.excalidraw.png)](../../../img/feat-design-1706-data_ingestion_gitlab_architecture_aws.excalidraw.png){:target="_blank"}

*File editable using [Excalidraw](https://excalidraw.com/) simply import the PNG, it contains scene data.*

### GCP deployment

TODO: Something using [kube jobs](https://kubernetes.io/docs/concepts/workloads/controllers/job/) could be used ?
*Spike currently in progress.*

## Docker images ?

TODO: list docker images that will be used for this pipeline, maybe juste 1 python image with necessary tools.


### Normalization method `normalized(NAMESPACE)`


## Environnement variable settings


### CI / CD settings

|Environment variable name | Default | Allowed values | Description |
|--- |--- |--- |--- |
| `sample`| `sample` | `sample` | sample |


### Lambda environment variables ?

|Environment variable name | Default | Allowed values | Description |
|--- |--- |--- |--- |
| `sample`| `sample` | `sample` | sample |


## Technical change that should be made

### Breaking changes

* TODO


### Other changes
* TODO
