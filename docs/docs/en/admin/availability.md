---
title: High Availability
---
[//]: # (Traduit avec Google Translate et Reverso)
# High Availability

This page is intended to provide advice and feedback on the
_high availability_ (or _HA - High Availability_) configurations of Tock bots and platforms.

> Coming soon: more details on how to achieve high availability on the different
>Tock components, and feedback on our use in production for several years
>(see [showcase / users](../about/showcase.md)).

## Redundancy and resilience

A single instance of `tock/build_worker` must exist.

It is recommended to use a single instance of `tock/bot_admin` and `tock/kotlin_compiler`.

For other components, especially the bot component (not provided) but also `tock/nlp_api` and
`tock/duckling`, it is recommended to deploy multiple instances to ensure better availability
or even better performance.

## Performance

As indicated in the [installation](../admin/installation.md) section, the first parameter to monitor is
available memory.

At high load - we have experienced more than 80 req/s on our own bots -
the limiting factor becomes the MongoDB database, which must then be resized accordingly
when the need arises.