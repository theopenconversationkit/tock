---
title: Installation
---
[//]: # (Traduit avec Google Translate et Reverso)
# Tock Installation

The [architecture](../../admin/architecture.md) page presents the Tock functional and technical architecture, the role of the
different components as well as the different deployment modes.

This chapter presents the different Tock installation options. In particular, it is about discussing the case of a
production installation as well as sharing some feedback on performance, resilience,
Tock's ability to scale, _Cloud_ type deployments, monitoring, etc.

> If you are only looking to test Tock with non-sensitive data, you may prefer to use the
> [Tock demo platform](https://demo.tock.ai).

## Installation with Docker

The [`tock-docker`](https://github.com/theopenconversationkit/tock-docker) repository provides a complete implementation of
Tock for the [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/) technologies.

Tock is composed by default of several Docker containers/images and a [MongoDB](https://www.mongodb.com) database.

For more information on installing Tock with Docker, see the instructions in the [`tock-docker`](https://github.com/theopenconversationkit/tock-docker) repository.

> The [deploy Tock with Docker](../../guides/platform.md) guide in the _Discover Tock_ section gives an example of deploying
> a complete platform in a few minutes with a minimal footprint using Docker and Docker Compose.
> However, this method is not feasible for a long-term deployment such as a production platform.

If you want to use Docker Compose in production, please read this [article](https://docs.docker.com/compose/production/)
and review the configuration, which is only given in the `tock-docker` project as an example.
In particular, the configuration of MongoDB instances should be reviewed carefully.

## Installation without Docker

It is entirely possible to install Tock without using Docker. By analyzing the descriptors provided in
[`tock-docker`](https://github.com/theopenconversationkit/tock-docker) (ie. the `pom.xml` files, the `Dockerfile`
and `docker-compose.yml`) one can easily design an installation without Docker.

Except for the MongoDB database, all other components can be started like classic Java/JVM applications, for example:

- directly from the command line
- within a Java application server
- from an integrated development tool (IDE)
- etc.

To learn more about the launch parameters of the different Tock components, you can take inspiration
from the commands present in the `tock-docker` descriptors or from the configurations provided for IntelliJ
(see below).

### Command line

One technique is to gather the different dependencies and JAR archives in a folder and then start the
component or application with a classic Java command.

For example, the component descriptor `tock-docker-nlp-api`
(see [`pom.xml`](https://github.com/theopenconversationkit/tock-docker/blob/master/nlp-api/pom.xml))
with the following command:

```shell
java $JAVA_ARGS -Dfile.encoding=UTF-8 -cp '/maven/*' ai.tock.nlp.api.StartNlpServiceKt
```

### Executable JAR

This is not the technique we recommend, but it is possible to run a single JAR containing all
dependencies (sometimes called _"fat JAR"_). Here is how to create such a JAR, using the example of the
Tock NLP-API component.

In the component POM (`nlp/api/service/pom.xml`), add the following declaration:

```xml
    <build>
        <plugins>
            <plugin>
                <artifactId>maven-assembly-plugin</artifactId>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                        <configuration>
                            <archive>
                                <manifest>
                                    <mainClass>ai.tock.nlp.api.StartNlpServiceKt</mainClass>
                                </manifest>
                            </archive>
                            <descriptors>
                                <descriptor>src/main/assembly/jar-with-dependencies.xml</descriptor>
                            </descriptors>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

Also create an archive descriptor `nlp/api/service/src/main/assembly/jar-with-dependencies.xml` with the following content:

```xml
<assembly xmlns="http://maven.apache.org/ASSEMBLY/2.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/ASSEMBLY/2.0.0 http://maven.apache.org/xsd/assembly-2.0.0.xsd">
    <id>jar-with-dependencies</id>
    <formats>
        <format>jar</format>
    </formats>
    <includeBaseDirectory>false</includeBaseDirectory>
    <dependencySets>
        <dependencySet>
            <outputDirectory>/</outputDirectory>
            <useProjectArtifact>true</useProjectArtifact>
            <unpack>true</unpack>
            <scope>runtime</scope>
        </dependencySet>
    </dependencySets>
    <containerDescriptorHandlers>
        <containerDescriptorHandler>
            <!-- Merge service implementations from dependencies -->
            <handlerName>metaInf-services</handlerName>
        </containerDescriptorHandler>
    </containerDescriptorHandlers>
</assembly>
```

Finally, build the _"jar-with-dependencies"_ archive with `mvn package`.

### In an IDE

For development, it is possible to run the different Tock components (NLU, Studio, bot...)
from an IDE like [IntelliJ](https://www.jetbrains.com/idea/), [Eclipse](https://www.eclipse.org/) or
[Visual Studio Code](https://code.visualstudio.com/) for example.

In addition to the [Docker images](https://github.com/theopenconversationkit/tock-docker/blob/master/docker-compose.yml),
configurations for IntelliJ are provided with the Tock sources:

- Configuration [Full _Tock Studio_ services (Bot + NLP) / `BotAdmin`](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/BotAdmin.xml)
- Configuration [_Tock Studio_ services (NLP only) / `Admin`](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/Admin.xml)
- Configuration [NLP service / `NlpService`](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/NlpService.xml)
- Configuration [Entity Service / `Duckling`](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/Duckling.xml)
- Configuration [NLP model construction service / `BuildWorker`](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/BuildWorker.xml)
- Configuration [Script compilation service / `KotlinCompilerServer`](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/KotlinCompilerServer.xml)

Finally, to launch the user interfaces (_Tock Studio_), the commands are described in the following link:

- Instructions [Full _Tock Studio_ interface (Bot + NLP)](https://github.com/theopenconversationkit/tock/blob/master/bot/admin/web/README)

## MongoDB database

### *replica set* architecture

The MongoDB database must be configured in _replica set_, because Tock takes advantage of [_change streams_](https://docs.mongodb.com/manual/changeStreams/).

> This implies that at least 3 _nodes_ must be deployed, which improves resilience.

Different scenarios are possible for the database:

- Install MongoDB nodes on one or more servers (classic method)
- Instantiate MongoDB nodes with Docker (for testing or local development)
- Use a MongoDB _cloud_ service in _SaaS (Software-as-a-Service)_, for example
[MongoDB Atlas](https://www.mongodb.com/cloud/atlas) available on AWS, Azure and GCP

> A [_replica set_ installation tutorial](https://docs.mongodb.com/manual/tutorial/deploy-replica-set/)
> is available on the MongoDB website.

### Data retention

Tock stores different types of data in its database and applies [_TTL (Time To Live)_](https://en.wikipedia.org/wiki/Time_to_live),
so that some expire and are purged automatically after a certain time.

> In practice, environment variables and the application of _TTL_ occur when the _DAO
> (Data Access Object)_ components are initialized, when Tock starts.

Tock's _TTL_ have a default value and are configurable using environment
variables. Some concern a specific Tock component, others must be defined on
several components.

> Since Tock can be used as a complete conversational platform or
> only the NLU/NLP part, we indicate the variables specific to conversational (denoted _Bot_)
> or usable on all types of platforms (denoted _\*_).

| Platform(s) | Environment variable | Default value | Description | Affected component(s) |
| ------------- | -------------------------------------------------------------- | ------------------------ | ---------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------- |
| _\*_ | `tock_nlp_classified_sentences_index_ttl_days` | `-1` (no expiration) | Unvalidated sentences (_Inbox_). | `nlp_api`, `nlp_admin`/`bot_admin`, `worker` |
| _\*_ | `tock_nlp_classified_sentences_index_ttl_intent_names` | Empty (all intents) | Unvalidated sentences (_Inbox_) >> restriction to certain intents, separated by commas.<br><em>(Example below).</em> | `nlp_api` |
| _\*_ | `tock_nlp_log_index_ttl_days` | `7` | NLP logs: sentence, intents, scores, entity details, etc. | `nlp_api` |
| _\*_ | `tock_nlp_log_stats_index_ttl_days` | `365` | NLP statistics: number of occurrences of a sentence, scores, etc. | `nlp_api` |
| _\*_ | `tock_user_log_index_ttl_days` | `365` | Log of actions in _Tock Studio_: _Stories_ changes, etc. | `nlp_admin`/`bot_admin` |
| _Bot_ | `tock_bot_alternative_index_ttl_hours` | `1` | Index on label alternatives (_Answers_). | `bot`/`bot_api` |
| _Bot_ | `tock_bot_dialog_index_ttl_days` | `7` | Conversations (_Analytics > Users/Search_). | `bot`/`bot_api`, `nlp_admin`/`bot_admin` |
| _Bot_ | `tock_bot_dialog_max_validity_in_seconds` | `60 * 60 * 24` (24h) | Conversation contexts (current intention, entities on the _bus_, etc.). | `bot`/`bot_api`, `nlp_admin`/`bot_admin` |
| _Bot_ | `tock_bot_flow_stats_index_ttl_days` | `365` | Browsing statistics (_Analytics > Activity/Behavior_). | `bot`/`bot_api`, `nlp_admin`/`bot_admin` |
| _Bot_ | `tock_bot_timeline_index_ttl_days` | `365` | User profiles/history: preferences, locale, last login, etc. <em>(excluding conversation details)</em> | `bot`/`bot_api`, `nlp_admin`/`bot_admin` |

Depending on the deployment mode used, these environment variables can be added either
directly on the command line, or in a descriptor such as `docker-compose.yml`, `dockerrun.aws.json` or other
(example below).

It is possible to automatically remove unvalidated sentences (_Inbox_) for certain intents only,
thanks to `tock_nlp_classified_sentences_index_ttl_intent_names` :

- {: data-hl-lines="6 7"} docker-compose.yml


```yaml
version: "3"
services:
  admin_web:
    image: tock/bot_admin:$TAG
    environment:
      - tock_nlp_classified_sentences_index_ttl_days=10
      - tock_nlp_classified_sentences_index_ttl_intent_names=greetings,unknown
```

- {: data-hl-lines="8 9"} dockerrun.aws.json

```json
{
  "AWSEBDockerrunVersion": 2,
  "containerDefinitions": [
    {
      "name": "admin_web",
      "image": "tock/bot_admin:${TAG}",
      "environment": [
        {
          "name": "tock_nlp_classified_sentences_index_ttl_days",
          "value": "10"
        },
        {
          "name": "tock_nlp_classified_sentences_index_ttl_intent_names",
          "value": "greetings,unknown"
        }
      ]
    }
  ]
}
```

{: .tabbed-code}

In this example, only sentences detected as `greetings` or `unknown` intents (but not validated) will be
deleted after `10` days; other sentences will not be deleted.

Only sentences validated by a user in _Tock Studio_, integrating the bot's NLP model, never expire
by default (even if it is still possible to delete them from the model via the _Search > Status: Included in model_ view):
it is therefore important not to validate sentences containing personal data for example.

> Data retention, encryption and anonymization are essential to protect
> data, especially if it is personal.
> For more information, see the [_Security > Data_](../../admin/security.md#data) section.

## Application Components

Depending on Tock's application components, whether mandatory or optional, some must be _single-instance_ and others
can be deployed in _multiple instances_ (see the [high availability](availability.md) section for more information).

For convenience, the components below are named after the [Docker](https://www.docker.com/) images provided
with Tock, although using Docker is not required to install Tock.

### Network Exposure

By default, the components or _containers_ of the Tock platform must not be exposed outside the
[_VPN_](https://fr.wikipedia.org/wiki/R%C3%A9seau_priv%C3%A9_virtuel) or [_VPC_](https://fr.wikipedia.org/wiki/Nuage_Priv%C3%A9_Virtuel).
Only the bot itself must be accessible to the partners and external channels with which we want to
integrate, for the functioning of the _WebHooks_.

| Component / Image | Network exposure | Description |
| ----------------------------------------------------------------------- | ---------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| [`tock/bot_admin`](https://hub.docker.com/r/tock/bot_admin) | VPN / VPC only | Interfaces and tools _Tock Studio_ |
| [`tock/build_worker`](https://hub.docker.com/r/tock/build_worker) | VPN / VPC only | Automatically rebuilds models whenever needed |
| [`tock/duckling`](https://hub.docker.com/r/tock/duckling) | VPN / VPC only | Parses dates and primitive types using [Duckling](https://duckling.wit.ai) |
| [`tock/nlp_api`](https://hub.docker.com/r/tock/nlp_api) | VPN / VPC only | Parses sentences from models built in _Tock Studio_ |
| [`tock/bot_api`](https://hub.docker.com/r/tock/bot_api) | VPN / VPC only | API for developing bots ([_Tock Bot API_](../dev/bot-api.md) mode) |
| [`tock/kotlin_compiler`](https://hub.docker.com/r/tock/kotlin_compiler) | VPN / VPC only | (Optional) Script compiler to enter them directly in the [_Build_](../user/studio/stories-and-answers.md) interface of _Tock Studio_ |
| bot (not provided) | Internet / partners | The bot itself, implementing the programmatic journeys, accessible to external partners/channels via _WebHooks_ |

Of course, the implementation of the bot itself is not provided with Tock (everyone implements their own features for their needs).

### HTTP Proxies

The [Java System Properties](https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html)
`https.proxyHost`, `http.proxyHost`, and `http.nonProxyHosts` are the recommended way to configure a proxy.

### Bot Packaging

A sample bot in [_Tock Bot Embedded_](../dev/bot-integre.md) mode is available in
[`docker-compose-bot-open-data.yml`](https://github.com/theopenconversationkit/tock-docker/blob/master/docker-compose-bot-open-data.yml).

Examples and guidelines for packaging bots in [_Tock Bot API_](../../dev/bot-api.md) mode (_WebHooks_, _WebSockets_) will be available soon.

## Minimum configurations

Tock architecture is composed of several components that can be deployed together on the same server,
or distributed across multiple machines/instances.

The main parameter to monitor is the available RAM.

### Model building

The larger your models, the more memory is needed to rebuild the models
(`tock/build_worker` component).

To give an order of magnitude, a model of 50,000 sentences with several intents, comprising about twenty entities,
will require provisioning about 8 GB of RAM for the `tock/build_worker` component.

However, large models with few entities can easily run with only 1 GB of RAM.

### JVM & Docker Memory

To ensure that Docker containers/instances do not exceed the available memory, it is recommended
to limit the memory of JVMs by following the following example:

```shell
JAVA_ARGS=-Xmx1g -XX:MaxMetaspaceSize=256m
```

## Machine optimization

It is possible to optimize deployments and infrastructures by taking into account different elements
such as:

- the needs of the respective components in machine resources: CPU, memory, disk
- the interest of having one or more instances of each component according to its role
- the constraints/objectives of resilience and high availability
- the cost models, particularly among public cloud providers

### Examples

For information, here are some examples of configurations currently in production.
These are the "application" components of the Tock architecture without the MongoDB database.

> EC2 instance types are for reference only. Tock has no dependencies on AWS.
> For more information, see the [AWS documentation](https://aws.amazon.com/ec2/pricing/on-demand/).

#### Limited Size Models

| Tock Components | Number of Instances | Number of CPUs or vCPUs | RAM | Example EC2 Instance Type |
| ------------------------------------------------------------- | ------------------ | --------------------- | ----------- | ---------------------------- |
| `admin-web` + `build-worker` + `kotlin-compiler` + `duckling` | 1 | 2 | 4 GB | `t3a.medium` (general purpose) |
| `bot` + `nlp-api` + `duckling` | 3 | 2 | 4 GB | `t3a.medium` (general purpose) |

#### Large Models

| Tock Components | Number of Instances | Number of CPUs or vCPUs | RAM | Example EC2 Instance Type |
| ------------------------------------------------------------- | ------------------ | --------------------- | ----------- | ------------------------------- |
| `admin-web` + `build-worker` + `kotlin-compiler` + `duckling` | 1 | 2 | 16 GB | `r5a.large` (memory optimized) |
| `bot` + `nlp-api` + `duckling` | 3 | 2 | 4 GB | `t3a.medium` (general purpose) |

## Frequently Asked Questions

### Making the administration interface available in a subdirectory

By default, the administration interface is served at the root (Example: `https://[domain host]`)
If you want to make it available on a relative path (`https://[domain host]/tock`),
use in the configuration of the docker image `tock/bot_admin` the environment variable `botadminverticle_base_href`.

For example: `botadminverticle_base_href=tock`

For `tock/nlp_admin`, you must use the property `adminverticle_base_href`.

## See also...

For a production use of Tock, we recommend you to also browse the following pages:

- [Security](../admin/security.md)
- [Monitoring](../admin/supervision.md)  
- [Cloud](../admin/cloud.md)
- [High availability](../admin/availability.md)
