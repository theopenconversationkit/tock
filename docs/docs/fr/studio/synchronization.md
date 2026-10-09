---
title: Synchronisation
---

# L'écran _Synchronization_

L'écran _Settings_ > _Synchronization_ copie les stories, les intentions et l'entraînement d'un bot (la source) vers un autre bot
(la cible), éventuellement dans un autre namespace. Par exemple, vous pouvez développer et tester de nouvelles stories sur un bot
de pré-production, puis les copier vers le bot de production, ou copier les phrases reçues par un bot de production vers un bot
de pré-production pour les qualifier.

![Synchronisation](../../img/synchronization.png "Écran de synchronisation")

Choisissez le namespace et l'application de la source et de la cible, puis lancez la synchronisation.

> Cet écran nécessite l'accès à plusieurs namespaces : l'administrateur de la plateforme doit donner la valeur `true`
> à la propriété `tock_namespace_open_access` (voir [Configuration](../operate/configuration.md#environnement)).

## Ce qui est synchronisé

- **Stories et intentions** : les stories de la source et leurs intentions sont copiées vers la cible.

    > **Attention :** les stories de la cible qui n'existent pas dans la source sont supprimées, et les autres sont
    > écrasées par les stories de la source.

- **Entraînement** : les phrases qualifiées de la source écrasent celles de la cible. Seules les phrases qui existent dans la
  source sont concernées : les autres phrases de la cible sont conservées. Par exemple, si la cible associe « bonjour »
  à l'intention `greetings` et que la source l'associe à l'intention `test`, la cible l'associe désormais à `test`.

- **Phrases à qualifier** (option _Copy Inbox Messages_) : les phrases pas encore qualifiées sont aussi copiées, par exemple
  pour qualifier sur un bot de pré-production les phrases reçues en production.
