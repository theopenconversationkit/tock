# Supervision

Ce chapitre présente quelques aspects supervision et _monitoring_ du fonctionnement de la plateforme 
et des bots Tock.

> A venir : plus de détails sur la manière de monitorer les bots, voire des exemples de dashboards pour quelques 
>technologies de monitoring classiques. N'hésitez pas à partager les vôtres.

## Lignes de vie (healthchecks)

L'url `/healthcheck` renvoie une code `HTTP 200` si tout est correct.

Pour certaines images, le ligne de vie peut ne pas être présente à la racine. En particulier :
 
- Pour `tock/admin`, la ligne de vie est localisée par défaut dans `/rest/admin/healthcheck` 
- Pour `tock/nlp_api` , la ligne de vie est `/rest/nlp/healthcheck` 
 
## Journalisation (logs)

### Logs applicatifs

Tock utilise [SLF4J](http://www.slf4j.org) pour générer ses logs. 

### Chiffrage et anonymisation

Voir la page [sécurité](security.md) concernant les possibilités de chiffrage et anonymisation des logs.
