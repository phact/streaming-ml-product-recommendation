#StreamingMLProductRecommendation

Example for using DSE Streaming analytics for product recommendations

##Usage:

###Prep the files:

Make sure dsefs is turned on `dse.yaml` 

    dsefs_option
        enabled: true

And push the raw data file into the root directory of dsefs:

```
dsefs / > put ./sales_observations sales_observations
dsefs / > ls sales_observations
sales_observations
```

###Streaming Job:
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

###Docs

pull in your submodules

    git submodule update --init
    git submodule sync

then run the server

    hugo server ./content

