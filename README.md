# StreamingMLProductRecommendation

This is a guide for how to use the power tools machine learning streaming product recommendation asset brought to you by the Vanguard team.

### Motivation

Machine learning powered recommendation engines have wide applications across multiple industries as companies seeking to provide their end customers with deep insights by leveraging data in the moment. Although there are many tools that allow for historical analysis that yield recommendations, DataStax Enterprise (DSE) is particularly well suited to power real-time recommendation / personalization systems. It is when it comes to operationalizing and productionizing analyitical systems that DSE will prove most useful. This is largely due to DSEs design objectives of operating at scale, in a distributed fashion, and while fulfilling performance and availability requirements required for user facing, mission critical applications.

### What is included?

This field asset includes a working application for real-time recommendations leveraging the following DSE functionality:

* Machine Learning
* Streaming analytics
* Batch analytics
* Real-time JDBC / SQL (dynamic caching)
* DSEFS

### Business Take Aways

By streaming customer market basket data from a retail organization through DSE analytics and using it to train a Collaborative Filterning Machine Learning model, we are able to maintain a top K list of recommended products by customer that reflect their historical and recent buying patterns.

In the retail industry, both online and brick and mortar businesses are leveraging ML and real-time analytics pipelines to gather insights that become differentiators for them in the marketplace. The DataStax stack is the foundation for enterprise personalization / recommendation systems across multiple industries.

### Technical Take Aways

For a technical deep dive, take a look at the following sections:

- Machine learning model
- Streaming analytics pipeline
- Real-time JDBC / SQL (dynamic caching)

##Startup Script

This Asset leverages
[simple-startup](https://github.com/jshook/simple-startup). To start the entire
asset run `./startup all` for other options run `./startup`

## Manual Usage:

### Prep the files:

Make sure dsefs is turned on `dse.yaml` 

    dsefs_option
        enabled: true

And push the raw data file into the root directory of dsefs:

```
dsefs / > put ./sales_observations sales_observations
dsefs / > ls sales_observations
sales_observations
```

### Streaming Job:
To run this on your local machine, you need to first run a Netcat server

    $ nc -lk 9999

Build:

    mvn package

and then run the example:

    $ dse spark-submit --deploy-mode cluster --supervise  --class
    com.datastax.powertools.analytics.SparkMLProductRecommendationStreamingJob
    ./target/StreamingMLProductRecommendations-0.1.jar localhost 9999

To run the  model, predict via streaming, and serve results via JDBC, run the
ServeJDBC class

    $ dse spark-submit --class
    com.datastax.powertools.analytics.SparkMLProductRecommendationServeJDBC
    ./target/StreamingMLProductRecommendations-0.1.jar localhost 9999


    $ dse beeline

    > !connect jdbc:hive2://localhost:10000

    > select * from recommendations.predictions where user=10277 order by prediction desc;


Into the `nc` prompt paste a few records and see the change in beeline:

```
102779564.0000
1027795649564.0000
1027515254.0000
1027795649564744.0000
10277956495647442304.0000
102751525415254.0000
102779564956474423042304.0000
10277   221     4
10277   221     1
```

Alternatively, you can run `./socketstream` to write a record per second to the stream from bash


### Docs

pull in your submodules

    git submodule update --init
    git submodule sync

then run the server

    hugo server ./content

