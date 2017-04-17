#StreamingMLProductRecommendation

Example for using DSE Streaming analytics for product recommendations

##Usage:

To run this on your local machine, you need to first run a Netcat server

    $ nc -lk 9999

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
