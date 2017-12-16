# Evaluer la pertinence du modèle NLP

## Données disponibles

Cinq onglets permettent de contrôler la pertinence du modèle :

1. **Stats** : statistiques minimales qui permettent de suivre la qualité du modèle en production. Elles comprennent:
    * l'auto-évaluation du modèle sur sa pertinence en terme de reconnaissance d'intention et d'entités
    * le nombre d'appels et le nombre d'erreurs
    * le temps moyen d'exécution 
2. **Test Trend** : évolution de la pertinence des [tests partiels de modèle](#tests-partiels-de-modele) 
3. **Intent Errors** : la liste des erreurs d'intention (vraies ou fausses) trouvées lors des tests partiels de modèle
4. **Entity Errors** : la liste des erreurs d'entité (vraies ou fausses) trouvées lors des tests partiels de modèle
5. **Model Builds** : la liste des constructions des modèles avec notamment le type de modèle, le nombre de phrases et la durée de construction

## Tests partiels de modèle

Les tests partiels de modèle constituent un moyen classique de détecter les erreurs de qualification,
ou les problèmes de proximité des intentions (ou entités) entre elles.

Il s'agit de prendre une partie du modèle actuelle au hasard (par exemple 90% des phrases du modèle) afin de construire
un modèle légèrement moins pertinent, puis de tester les 10% restant avec ce nouveau modèle.

Une fois le principe posé, il ne reste plus qu'à répéter le processus un certain nombre de fois
pour que les erreurs les plus fréquentes soient présentées à un correcteur manuel.

Ces tests partiels ne présentent une utilité qu'avec des modèles déjà conséquents.

### Lancement des tests

Par défaut les tests sont programmés pour être lancés de minuit à 5h du matin, toutes les 10 minutes.
Il est possible de configurer ce comportement avec la propriété *tock_test_model_timeframe* (=0,5 par défaut)

### Intentions en erreur

C'est l'objet de l'onglet **Intent Errors**. Voici par exemple une copie d'écran pour le modèle bot open-data.

![schéma Tock](img/intent-errors.png "Erreur d'intentions détectées")

Comme il s'agit d'un modèle d'exemple, aucune *vraie* erreur n'a été détectée. 
On peut tout de même constater que dans certains cas le modèle se trompe systématiquement avec une probabilité élevée.

Pour chaque phrase il est possible via la colonne *Actions* de confirmer que le modèle de base est correct (*Validate Intent*) 
ou de corriger l'éventuelle erreur détectée (*Change The Intent*). 

### Entités en erreur

Elles sont consultables de manière symétrique via l'onglet **Entity Errors**. 

![schéma Tock](img/entity-errors.png "Erreur d'entités détectées")