# Installer et configurer Tock en production

## Composants

Tock est composé par défaut de plusieurs images dockers et d'une base de donnée, [MongoDB](https://www.mongodb.com).

Un exemple de configuration complète sous forme de fichier *docker-compose* est disponible dans le projet [tock-docker](https://github.com/voyages-sncf-technologies/tock-docker) : 
[docker-compose-bot-open-data.yml](https://github.com/voyages-sncf-technologies/tock-docker/blob/master/docker-compose-bot-open-data.yml)

Si vous souhaitez utiliser docker-compose en production, merci de lire cet [article](https://docs.docker.com/compose/production/) 
et de revoir la configuration, qui est uniquement donnée dans le projet *tock-docker* à titre d'exemple d'environnement de dévelopement. 
En particulier, la configuration des instances MongoDB devra être revue attentivement. 

### MongoDB

La base Mongo doit être configurée "en replica set", c'est à dire avec au minimum 3 instances déployées.
C'est obligatoire car Tock utilise la fonctionnalité des [Change Streams](https://docs.mongodb.com/manual/changeStreams/)
qui a comme pré-requis l'installation en replica set.

Il s'agit également d'une bonne pratique afin d'assurer une haute disponibilité de la base de données. 

Un [tutoriel d'installation en replica set](https://docs.mongodb.com/manual/tutorial/deploy-replica-set/)
 est disponible sur le site de MongoDB.
 
### Liste et rôles des images dockers

Voici les images composants Tock. 
Aucune de ces images ne doit être exposée à l'extérieur par défaut.

- [tock/bot_admin](https://hub.docker.com/r/tock/bot_admin) : l'interface d'administration de Tock.  
- [tock/kotlin_compiler](https://hub.docker.com/r/tock/kotlin_compiler): Une image docker facultative 
qui permet de compiler les scripts du bot via l'interface d'administration.
- [tock/build_worker](https://hub.docker.com/r/tock/build_worker): L'image docker qui permet de reconstruire
les modèles NLP quand cela se révèle nécessaire.
- [tock/nlp_api](https://hub.docker.com/r/tock/nlp_api) Permet au bot d'analyser les phrases à partir des modèles
construits via l'interface d'administration.
- [tock/duckling](https://hub.docker.com/r/tock/duckling) Permet d'analyser et de valoriser les dates
et les types primitifs en utilisant le projet [duckling](https://duckling.wit.ai).

Enfin bien entendu, il est nécessaire d'installer une image représentant le bot lui-même,
image qui doit être accessible de l'extérieur.

Un exemple de configuration de cette dernière image se trouve dans le fichier 
[docker-compose-bot-open-data.yml](https://github.com/voyages-sncf-technologies/tock-docker/blob/master/docker-compose-bot-open-data.yml).

## Recommandations
 
### Configuration minimale requise

Le paramètre principal à surveiller est la mémoire vive disponible.

En particulier, plus vos modèles sont importants, plus il sera nécessaire d'augmenter la mémoire pour reconstruire les modèles
dans l'image *tock/build_worker*.

Pour donner un ordre de grandeur, un modèle de 50000 phrases avec plusieurs intentions comportant une vingtaine d'entités chaque
nécessitera de provisionner environ 8Go de RAM pour l'image *tock/build_worker*.

Cependant des modèles importants, mais avec peu d'entités, tiennent facilement en 1Go de RAM.

### Configuration de la mémoire des JVMs

Pour garantir que les instances dockers ne dépassent pas la mémoire disponible, il est recommandé
de limiter la mémoire disponible pour la JVM en suivant l'exemple suivant:

```
JAVA_ARGS=-Xmx1g -XX:MaxMetaspaceSize=256m
```

### Haute disponibilité

Autant il est important de s'assurer qu'une seule instance est installée pour *tock/build_worker*
- une seule instance est également recommandée pour *tock/bot_admin* et *tock/kotlin_compiler* -,
autant il est recommandé d'installer plusieurs instances du bot mais également de *tock/nlp_api* et de *tock/duckling*
afin de s'assurer d'une haute disponibilité.

A forte charge - nous avons expérimenté plus de 80 req/s sur nos propres bots - 
le facteur limitant devient la base de données MongoDB, qu'il faut alors redimensionner en conséquence
quand le besoin s'en fait sentir.

## Authentification

Tock supporte plusieurs systèmes d'authentification pour l'interface d'administration. 
Il utilise les librairies [vert.x](https://vertx.io/docs/vertx-auth-common/java/) correspondantes. 

Voici les systèmes disponibles par défaut :  

- Un modèle "par propriété", qui est utilisé par défaut. 
Le code est disponible ici : [PropertyBasedAuthProvider](https://github.com/voyages-sncf-technologies/tock/blob/master/shared/src/main/kotlin/security/auth/PropertyBasedAuthProvider.kt#L61)

- Un modèle OAuth2 dont un exemple est donné par la classe [GithubOAuthProvider](https://github.com/voyages-sncf-technologies/tock/blob/master/shared/src/main/kotlin/security/auth/GithubOAuthProvider.kt)

- Un modèle basé sur un token JWT pour une configuration Aws: [AWSJWTAuthProvider](https://github.com/voyages-sncf-technologies/tock/blob/master/shared/src/main/kotlin/security/auth/AWSJWTAuthProvider.kt)    

Si ces modèles ne correspondent pas à votre besoin, il est relativement simple d'en développer d'autres
en se basant sur les exemples ci-dessus. N'hésitez pas à contribuer au projet et à nous contacter pour toute question!

## Configuration de proxy http

L'ajout des [propriétés système java correspondantes](https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html)
*https.proxyHost*, *http.proxyHost* et *http.nonProxyHosts* est la méthode recommandée pour configurer un proxy.

## Encryption et Obfuscation

### Encryption

Il est recommandé de déployer vos MongoDB [en mode encrypté](https://docs.mongodb.com/manual/tutorial/configure-encryption/).

Vous pouvez cependant ajouter une encryption applicative facultative de certains champs en base de données.

C'est le rôle de la propriété `tock_encrypt_pass` qui vous permet d'indiquer un mot de passe
permettant d'encrypter et de décrypter ces champs - par défaut Tock encrypte toutes les données utilisateurs
jugées sensibles si ce mot de passe est spécifié. Pour plus de détails, consultez le [code source](https://github.com/voyages-sncf-technologies/tock/blob/master/shared/src/main/kotlin/security/Encryptors.kt).

### Obfuscation

Par ailleurs, il peut être souhaitable que certaines phrases soient affichées dans les logs
 ou dans l'interface d'administration de manière *obfusquée*. 
 Ce serait par exemple le cas de numéros confidentiels de cartes de fidélité  qui ne devraient être lues, ni
 par vos administrateurs systèmes, ni par les utilisateurs de l'interface d'administration. 
  
 Tock met à disposition un système basé sur les expressions régulières dont l'interface de base
 est [StringObfuscator](https://github.com/voyages-sncf-technologies/tock/blob/master/shared/src/main/kotlin/security/StringObfuscator.kt).
 
## Monitoring

### Logs

Tock utilise [SLF4J](www.slf4j.org). 

### Ligne de vie

L'url `/healthcheck` renvoie une code HTTP 200 si tout est correct. 

Pour certaines images, le ligne de vie peut ne pas être présente à la racine. En particulier :
 
- Pour `tock/admin`, la ligne de vie est localisée par défaut dans `/rest/admin/healthcheck` 
- Pour `tock/nlp_api` , la ligne de vie est `/rest/nlp/healthcheck` 
 
## Contact

Pour toute question ou remarque, n'hésitez pas à nous contacter sur [gitter](https://gitter.im/tockchat/Lobby).  
