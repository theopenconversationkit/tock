---
title: Haute disponibilité
---

# Haute disponibilité

Cette page est destinée à fournir des conseils et des retours d'exéprience sur les configurations 
_haute disponibilité_ (ou _HA - High Availability_) de bots et plateformes Tock.

> A venir : plus de détails sur les manières d'obtenir une haute disponibilité sur les différents 
>composants Tock, et des retours sur notre utilisation en production depuis plusieurs années 
>(cf [vitrine / utilisateurs](../about/showcase.md)). 

## Redondance et résilience

Une seule instance de `tock/build_worker` doit exister.

Il est recommandé d'utiliser une seule instance de `tock/bot_admin` et `tock/kotlin_compiler`.
 
Pour les autres composants, en particulier le composant bot (non fourni) mais également `tock/nlp_api` et 
`tock/duckling`, il est recommandé de déployer plusieurs instances pour assurer une meilleure disponibilité 
voire de meilleures performances.

## Performance

Comme indiqué dans la section [installation](../admin/installation.md), le premier paramètre à surveiller est 
la mémoire disponible.

A forte charge - nous avons expérimenté plus de 80 req/s sur nos propres bots - 
  le facteur limitant devient la base de données MongoDB, qu'il faut alors redimensionner en conséquence
  quand le besoin s'en fait sentir.