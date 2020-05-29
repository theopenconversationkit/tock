# Installation Tock

La page [architecture](architecture.md) présente l'architecture fonctionnelle et technique Tock, le rôle des différents 
composants ainsi que les différents modes de déploiement.

Ce chapitre présente les différentes options d'installation de Tock. En particulier, il s'agit d'évoquer le cas d'une 
installation en production ainsi que partager quelques retours d'expérience sur les performances, la résilience, 
la capacité de Tock à monter en charge, les déploiementsde type _Cloud_, la supervision, etc.

> Si vous cherchez seulement à tester Tock avec des données non sensibles, vous pouvez préférer utiliser la 
>[plateforme de démonstration Tock](https://demo.tock.ai).

## Installation avec Docker

Le dépôt [`tock-docker`](https://github.com/theopenconversationkit/tock-docker) fournit une implémentation complète de 
Tock pour les technologies [Docker](https://www.docker.com/) et [Docker Compose](https://docs.docker.com/compose/). 
Tock est composé par défaut de plusieurs conteneurs/images Docker et d'une base de donnée [MongoDB](https://www.mongodb.com).

Pour en savoir plus sur l'installation de Tock avec Docker, voir les instructions du dépôt [`tock-docker`](https://github.com/theopenconversationkit/tock-docker).

> Le guide [déployer Tock avec Docker](../guide/plateforme.md) dans la section _Découvrir Tock_ donne un exemple de déploiement 
d'une plateforme complète en quelques minutes avec une empreinte minimale en utilisant Docker et Docker Compose. 
Cependant, cette méthode n'est pas envisageable pour un déploiement pérenne comme une plateforme de production.

Si vous souhaitez utiliser Docker Compose en production, merci de lire cet [article](https://docs.docker.com/compose/production/) 
et de revoir la configuration, qui est uniquement donnée dans le projet `tock-docker` à titre d'exemple. 
En particulier, la configuration des instances MongoDB doit être revue attentivement.

## Installation sans Docker

Il est tout à fait possible d'installer Tock sans utiliser Docker. En analysant les descripteurs fournis dans 
[`tock-docker`](https://github.com/theopenconversationkit/tock-docker) (ie. les fichiers `pom.xml`, les `Dockerfile` 
et `docker-compose.yml`) on peut facilement concevoir une installation sans Docker.

Hormis la base de données MongoDB, tous les autres composants peuvent démarrer comme des applications Java/JVM classiques, par exemple :

- directement en ligne de commande
- au sein d'un serveur d'applications Java
- depuis un outil de développement intégré (IDE)
- etc.

Pour en savoir plus sur les paramètres de lancement des différents composants Tock, vous pouvez vous inspirer 
des commandes présentes dans les descripteurs de `tock-docker` ou encore des configurations fournies pour IntelliJ  
(voir ci-dessous).

### Ligne de commande

Une technique consiste à rassembler les différentes dépendances et archives JAR dans un dossier puis démarrer le 
composant ou l'application avec une commande Java classique.

Pour exemple, le descripteur du composant `tock-docker-nlp-api` 
 (voir [`pom.xml`](https://github.com/theopenconversationkit/tock-docker/blob/master/nlp-api/pom.xml))
avec la commande suivante :

    java $JAVA_ARGS -Dfile.encoding=UTF-8 -cp '/maven/*' ai.tock.nlp.api.StartNlpServiceKt

### JAR exécutable

Ce n'est pas la technique que nous recommandons, mais il est possible d'exécuter un JAR unique contenant toutes les 
dépendances (parfois appelé _"fat JAR"_). Voici comment procéder pour créer un tel JAR, en reprenant l'exemple du 
composant Tock NLP-API.

Dans le POM du composant (`nlp/api/service/pom.xml`), ajoutez la déclaration suivante :

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

Créez également un descripteur d'archive `nlp/api/service/src/main/assembly/jar-with-dependencies.xml` avec le contenu suivant :

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

Pour finir, construisez l'archive _"jar-with-dependencies"_ avec `mvn package`.

### Dans un IDE

Pour le développement, il est possible d'exécuter les différents composants Tock (NLU, Studio, bot...) 
depuis un IDE comme [IntelliJ](https://www.jetbrains.com/idea/), [Eclipse](https://www.eclipse.org/) ou 
[Visual Studio Code](https://code.visualstudio.com/) par exemple.

Outre les [images Docker](https://github.com/theopenconversationkit/tock-docker/blob/master/docker-compose.yml),
des configurations pour IntelliJ sont fournies avec les sources de Tock :

- Configuration [Services _Tock Studio_ complets (Bot + NLP) / `BotAdmin`](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/BotAdmin.xml) 
- Configuration [Services _Tock Studio_ (NLP uniquement) / `Admin`](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/Admin.xml) 
- Configuration [Service NLP / `NlpService`](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/NlpService.xml)
- Configuration [Service d'entités / `Duckling`](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/Duckling.xml)
- Configuration [Service construction des modèles NLP / `BuildWorker`](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/BuildWorker.xml)
- Configuration [Service de compilation des scripts / `KotlinCompilerServer`](https://github.com/theopenconversationkit/tock/blob/master/.idea/runConfigurations/KotlinCompilerServer.xml)

Enfin pour lancer les interfaces utilisateur (_Tock Studio_), les commandes sont décrites dans les liens suivants :

- Instructions [Interface _Tock Studio_ complète (Bot + NLP)](https://github.com/theopenconversationkit/tock/blob/master/bot/admin/web/README.md)
- Instructions [Interface _Tock Studio_ (NLP uniquement)](https://github.com/theopenconversationkit/tock/blob/master/nlp/admin/web/README.md)

## Base de données MongoDB

La base MongoDB doit être configurée en _replica set_, car Tock tire parti des [_change streams_](https://docs.mongodb.com/manual/changeStreams/).  

> Cela implique qu'au minimum 3 _noeuds_ doivent être déployés, ce qui améliore la résilience.

Différents scénarios sont possibles pour la base de données :

- Installer les noeuds MongoDB sur un ou plusieurs serveurs (méthode classique)
- Instancier les noeuds MongoDB avec Docker (pour des tests ou le développement en local)
- Utiliser un service _cloud_ MongoDB en _SaaS (Software-as-a-Service)_, par exemple 
[MongoDB Atlas](https://www.mongodb.com/cloud/atlas) disponible sur AWS, Azure et GCP 

> Un [tutoriel d'installation en _replica set_](https://docs.mongodb.com/manual/tutorial/deploy-replica-set/)
 est disponible sur le site de MongoDB.

## Composants applicatifs

Selon les composants applicatifs de Tock, obligatoires ou facultatifs, certains doivent être _mono-instance_ et d'autres 
peuvent être déployés en _plusieurs instances_ (voir la section [haute disponibilité](disponibilite.md) pour en savoir plus).

Pour plus de commodité, les composants ci-dessous sont nommé comme les images [Docker](https://www.docker.com/) fournies 
avec Tock, bien que l'utilisation de Docker ne soit pas obligatoire pour installer Tock.

### Exposition réseau

Par défaut, les composants ou _conteneurs_ de la plateforme Tock ne doivent pas être exposés à l'extérieur du 
[_VPN_](https://fr.wikipedia.org/wiki/R%C3%A9seau_priv%C3%A9_virtuel) ou [_VPC_](https://fr.wikipedia.org/wiki/Nuage_Priv%C3%A9_Virtuel). 
Seul le bot lui-même doit être accessible des partenaires et canaux externes auxquels on veut 
s'intégrer, pour le fonctionnement des _WebHooks_.

| Composant / Image | Exposition réseau | Description |
|-------------------|-------------------|-------------|
| [`tock/bot_admin`](https://hub.docker.com/r/tock/bot_admin)             | VPN / VPC uniquement   | Interfaces et outils _Tock Studio_ |
| [`tock/build_worker`](https://hub.docker.com/r/tock/build_worker)       | VPN / VPC uniquement   | Reconstruit les modèles automatiquement dès que nécessaire |
| [`tock/duckling`](https://hub.docker.com/r/tock/duckling)               | VPN / VPC uniquement   | Analyse les dates et types primitifs en utilisant [Duckling](https://duckling.wit.ai) |
| [`tock/nlp_api`](https://hub.docker.com/r/tock/nlp_api)                 | VPN / VPC uniquement   | Analyse les phrases à partir des modèles construits dans _Tock Studio_ |
| [`tock/bot_api`](https://hub.docker.com/r/tock/bot_api)                 | VPN / VPC uniquement   | API pour développer des bots (mode [_Tock Bot API_](../dev/bot-api.md)) |
| [`tock/kotlin_compiler`](https://hub.docker.com/r/tock/kotlin_compiler) | VPN / VPC uniquement   | (Facultatif) Compilateur de scripts pour les saisir directement dans l'interface [_Build_](../utilisateur/studio/build.md) de _Tock Studio_ |
| bot (non fourni)                                                        | Internet / partenaires | Le bot lui-même, implémentant les parcours programmatiques, accessible des partenaires/canaux externes via des _WebHooks_ |

Bien sûr, l'implémentation du bot lui-même n'est pas fournie avec Tock (chacun implémente ses fonctionnalités propres pour son besoin).

### Proxies HTTP

Les [propriétés système Java](https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html)
`https.proxyHost`, `http.proxyHost` et `http.nonProxyHosts` sont la méthode recommandée pour configurer un proxy.

### Packaging du bot

Un exemple de bot en mode [_Tock Bot intégré_](../dev/bot-integre.md) est disponible dans 
[`docker-compose-bot-open-data.yml`](https://github.com/theopenconversationkit/tock-docker/blob/master/docker-compose-bot-open-data.yml).

Des exemples et indications pour packager des bots en mode [_Tock Bot API_](../dev/bot-api.md) (_WebHooks_, _WebSockets_) seront bientôt disponibles.

## Configurations minimales

L'architecture Tock est composée de plusieurs composants qui peuvent être déployés ensemble sur un même serveur,
ou répartis sur plusieurs machines/instances.

Le paramètre principal à surveiller est la mémoire vive disponible.

### Construction des modèles

Plus vos modèles sont importants, plus il est nécessaire d'augmenter la mémoire pour reconstruire les modèles
(composant `tock/build_worker`).

Pour donner un ordre de grandeur, un modèle de 50000 phrases avec plusieurs intentions, comportant une vingtaine d'entités,
nécessitera de provisionner environ 8 Go de RAM pour le composant `tock/build_worker`.

Cependant, des modèles importants mais contenant peu d'entités fonctionnent facilement avec seulement 1 Go de RAM.

### Mémoire JVM & Docker

Pour garantir que les conteneurs/instances Docker ne dépassent pas la mémoire disponible, il est recommandé
de limiter la mémoire des JVMs en suivant l'exemple suivant :

```
JAVA_ARGS=-Xmx1g -XX:MaxMetaspaceSize=256m
```

## Optimisation machines

Il est possible d'optimiser déploiements et infrastructures en prenant en compte différents éléments 
comme :

- les besoins des composants respectifs en ressources machines : CPU, mémoire, disque
- l'intérêt d'avoir une ou plusieurs instances de chaque composant suivant son rôle
- les contraintes/objectifs de résilience et haute disponibilité
- les modèles de coûts, notamment chez les fournisseurs de clouds publics

### Exemples

A titre indicatif, voici quelques exemples de configurations actuellement en production. 
Il s'agit des composants "applicatifs" de l'architecture Tock sans la base de donnée MongoDB.

> Les types d'instances EC2 sont donnés à titre indicatif. Tock n'a pas de dépendance à AWS.  
> Pour en savoir plus voir la [documentation AWS](https://aws.amazon.com/fr/ec2/pricing/on-demand/).

#### Modèles de taille limitée

| Composants Tock  | Nombre d'instances | Nombre de CPU ou vCPU | Mémoire RAM | Exemple type d'instance EC2 |
|------------------|--------------------|-------------------|-------------|-----------------------------|
| `admin-web` + `build-worker` + `kotlin-compiler` + `duckling`| 1 | 2 | 4 Go | `t3a.medium` (usage général) |
| `bot` + `nlp-api` + `duckling`| 3 | 2 | 4 Go | `t3a.medium` (usage général) |

#### Modèles de taille importante

| Composants Tock  | Nombre d'instances | Nombre de CPU ou vCPU | Mémoire RAM | Exemple type d'instance EC2 |
|------------------|--------------------|-------------------|-------------|-----------------------------|
| `admin-web` + `build-worker` + `kotlin-compiler` + `duckling`| 1 | 2 | 16 Go | `r5a.large` (mémoire optimisée) |
| `bot` + `nlp-api` + `duckling`| 3 | 2 | 4 Go | `t3a.medium` (usage général) |

## Voir aussi...

Pour une utilisation de Tock en production, nous vous recommandons de parcourir également les pages suivantes :

* [Sécurité](securite.md)
* [Supervision](supervision.md)
* [Cloud](cloud.md)
* [Haute disponibilité](disponibilite.md)
