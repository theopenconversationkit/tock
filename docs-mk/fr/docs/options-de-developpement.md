# Deux possibilités de développement

## Bot Intégré

Dans ce mode, vous accès à l'intégralité des fonctionnalités que met à disposition Tock
 pour développer un Bot. C'est de cette manière que sont développés aujourd'hui les bots publiés par
 les concepteurs de Tock. 
 
Cependant la phase de mise en place de la solution est assez complexe puisque il est nécessaire :

- D'installer une stack docker sur son poste ou sur son serveur
- De permettre la connexion partagée à la base MongoDB entre les poste de dev et la stack Tock utilisée
- De maîtriser le langage Kotlin

## Bot via API

Si vous souhaitez évaluer la solution Tock, il est conseillé d'utiliser les APIs (actuellement en phase béta) 
mises à disposition par Tock. Deux options s'offrent à vous.

### Installer Tock sur votre serveur

Vous installez la stack docker de Tock sur votre serveur. 

Vous conservez ainsi l'ensemble de vos données. 
Vos développeurs utilisent l'API pour se connecter à Tock. Ils n'ont donc pas besoin d'avoir accès à la base de données de Tock.

### Utiliser la plateforme de démonstration (disponible bientôt)

Vous pouvez également utiliser la plateforme de démonstration mutualisée de Tock comme serveur de référence.
Dans ce cas, il n'est pas nécessaire d'installer Tock sur vos serveurs.

A noter cependant que cet usage convient pour un environnement de développement 
et en aucun cas pour un environnement de production.