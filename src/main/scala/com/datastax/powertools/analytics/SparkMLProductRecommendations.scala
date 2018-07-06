package com.datastax.powertools.analytics


import com.datastax.powertools.analytics.ddl.DSECapable
import org.apache.spark.ml.recommendation.ALS
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SQLContext, SaveMode, SparkSession}
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.hive.thriftserver.HiveThriftServer2
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.cassandra._



// For DSE it is not necessary to set connection parameters for spark.master (since it will be done
// automatically)

/**
 * https://issues.apache.org/jira/browse/SPARK-6407
 */
object SparkMLProductRecommendations extends DSECapable {

  def main(args: Array[String]) {
    if (args.length < 2) {
      System.err.println("Usage: SparkMLProductRecommendationServeJDBC <hostname> <port>")
      System.exit(1)
    }

    // Create the context with a 1 second batch size
    val sc = connectToDSE("SparkMLProductRecommendation")

    // Set up schema
    setupSchema("recommendations", "predictions", "(user int, item int, preference float, prediction float, PRIMARY KEY((user), item))")

    setupSchema("recommendations", "user_ratings", "(user int, item int, preference float, PRIMARY KEY((user), item))")

    //Start sql context to read flat file
    val sqlContext = new SQLContext(sc)



    //train with batch file:
    //get the raw data
    val trainingData = sqlContext.read.format("com.databricks.spark.csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .option("delimiter", ":")
      .load("dsefs:///sales_observations")
      .cache()

    //Instantiate our estimator
    val algorithm = new ALS()
      .setMaxIter(5)
      .setRegParam(0.01)
      .setImplicitPrefs(true)
      .setUserCol("user")
      .setItemCol("item")
      .setRatingCol("preference")

    //train the Estimator against the training data from the file to produce a trained Transformer.
    val model = algorithm.fit(trainingData)

    val ssc = new StreamingContext(sc, Seconds(5))

    // Create a socket stream on target ip:port and count the
    // words in input stream of \n delimited text (eg. generated by 'nc')
    // Note that no duplication in storage level only for running locally.
    // Replication necessary in distributed scenario for fault tolerance.
    val observation = ssc.socketTextStream(args(0), args(1).toInt, StorageLevel.MEMORY_AND_DISK_SER)
    observation.print

    val testStream= observation.map(_.split(":")).filter(x => {
      (x.size >2 && x(0).forall(_.isDigit))
    }).map(x =>
        (x(0).toLong, x(1).toLong, x(2).toFloat)
    )

    //now predict against the live stream
    //this gives us predicted ratings for the item user combination fed from the stream
    //words.foreachRDD { (rdd: RDD[String], time: Time) =>
    testStream.foreachRDD { (rdd:RDD[(Long, Long, Float)], time: org.apache.spark.streaming.Time) =>
      // Get the singleton instance of SparkSession
      val spark = SparkSessionSingleton.getInstance(rdd.sparkContext.getConf)

      // Convert RDD[String] to RDD[case class] to DataFrame
      import spark.implicits._
      val testStreamDS = rdd.map((r:(Long,Long,Float)) => Rating(r._1, r._2, r._3)).toDS()

      //write the ratings to a user ratings table
      testStreamDS.write.cassandraFormat("user_ratings", "recommendations").mode(SaveMode.Append).save

      val predictions = model.transform(testStreamDS).cache();

      predictions.write.cassandraFormat("predictions", "recommendations").mode(SaveMode.Append).save

      predictions.map(x => "item: " + x.getLong(0)+ " user: " +  x.getLong(1) + " rating: " + x.getFloat(2)).show()
    }


    /*
    //Removing cache and serve functionality in favor of Always On Sql.
    // This snippet may be relevant for advanced cacheing scenarios

    //start the JDBC server to host predictions
    val hiveContext = new HiveContext(sc)
    HiveThriftServer2.startWithContext(hiveContext)

    //CACHE TABLE table recommendations.predictions once per window (which is once per RDD)
    observation.foreachRDD(row => {
      //hiveContext.sql("select * from recommendations.predictions").persist()
      //hiveContext.cacheTable("recommendations.predictions")
      hiveContext.sql("use recommendations").collect()
      hiveContext.sql("cache table predictions").collect()
      print(s"Cached the predictions table")
    })
    */

    ssc.start()
    ssc.awaitTermination()
  }

  var conf: SparkConf = _
  var sc: SparkContext = _
}

case class Predictions(user: Int, item: Int, prediction: Float)

/** Lazily instantiated singleton instance of SparkSession */
object SparkSessionSingleton {

  @transient  private var instance: SparkSession = _

  def getInstance(sparkConf: SparkConf): SparkSession = {
    if (instance == null) {
      instance = SparkSession
        .builder
        .config(sparkConf)
        .getOrCreate()
    }
    instance
  }
}

case class Rating(user: Long, item: Long, preference: Float)
// scalastyle:on println
