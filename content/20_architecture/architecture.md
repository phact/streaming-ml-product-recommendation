+++
date = "2017-04-10T14:21:00-05:00"
title = "Architecture"
weight = 3
+++

This section details the architecture demonstrated in this reference field asset.

### Architecture Diagram

<div title="rendered dynamically" align="middle">
{{< mermaid >}}
graph LR
C["Client"]
A["Queue"]--"Streaming predictors"-->B["DSE Streaming Analytics <br/>ML Pipeline"]
B--"microbatches"-->C["DSE Cassandra"]
B--"JDBC/SQL"-->D["Client"]
D--"JDBC/SQL"-->B
E["DSEFS"]--"Batch training"-->B
{{< /mermaid >}}
</div>

### Streaming

For simplicity, this demonstration uses sockets to push streaming data into DSE Streaming Analytics. We leverage `nc -lk 9999` to allow users to manually paste the data themselves into the sockets and thus feed it to the streaming program. We also automate this process in `startup.sh`

In a production setting, the source for the streaming application would be a message bus (i.e. kafka) to allow messages to queue up if they are not being processed and to allow message replay once streaming continues. In the case of Kafka, we recommend the Kafka direct API which leverages a stored Kafka offset with metadata checkpointing for HA. Other "Receiver" based approaches may require a WAL for data checkpointing. DSEFS can be leveraged as the distributed file system for both [metatata] checkpointing and WAL.
