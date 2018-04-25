---
title: Interpreting Outcomes
weight: 401
menu:
  main:
    parent: Interpreting Outcomes
    identifier: interpretingoutcomes
    weight: 41
---

This Asset kicks off a Spark Streaming Job that runs in the background and does real-time scoring for recommendations against streaming data.

This data is persisted in a DSE table that can be observed to see the output of the recommendations.

Use DSE Studio on your browser (it is running on the seed node of the cluster) to see some sample queries, both SQL (powered by alwayson-sql) and cql, that can be used to see the output of the reccomendations.
