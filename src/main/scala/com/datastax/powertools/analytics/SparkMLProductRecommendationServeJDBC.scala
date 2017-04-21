package com.datastax.powertools.analytics

import com.brkyvz.spark.recommendation.{LatentMatrixFactorization, StreamingLatentMatrixFactorization}
import com.datastax.powertools.analytics.SparkMLProductRecommendationBatchJob.setupSchema
import com.datastax.powertools.analytics.ddl.DSECapable
import com.datastax.spark.connector._
import com.datastax.spark.connector.SomeColumns
import org.apache.spark.ml.recommendation.ALS.Rating
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.hive.thriftserver.HiveThriftServer2
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}


// For DSE it is not necessary to set connection parameters for spark.master (since it will be done
// automatically)

/**
 * https://github.com/brkyvz/streaming-matrix-factorization
 * https://issues.apache.org/jira/browse/SPARK-6407
 */
object SparkMLProductRecommendationServeJDBC extends DSECapable {

  def main(args: Array[String]) {
    if (args.length < 2) {
      System.err.println("Usage: SimpleSparkStreaming <hostname> <port>")
      System.exit(1)
    }

    // Create the context with a 1 second batch size
    val sc = connectToDSE("SparkMLServeRecommendationJDBC")

    // Set up schema
    setupSchema("recommendations", "predictions", "(user int, item int, preference float, prediction float, PRIMARY KEY((user), item))")

    val sqlContext = new SQLContext(sc)
    val ssc = new StreamingContext(sc, Seconds(5))

    //start the JDBC server to host predictions
    val hiveContext = new HiveContext(sc)
    HiveThriftServer2.startWithContext(hiveContext)


    //train with batch file:
    //get the raw data
    val observations:RDD[Rating[Long]] = sqlContext.read.format("com.databricks.spark.csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .option("delimiter", "\t")
      .load("dsefs:///sales_observations").map(row =>
      Rating(row.getInt(0).toLong, row.getInt(1).toLong, row.getDouble(2).toFloat)
    )
      .cache()

    //train from the file
    val algorithm = new StreamingLatentMatrixFactorization()
    //val algorithm = new LatentMatrixFactorization()
    algorithm.trainOn(observations)



    // Create a socket stream on target ip:port and count the
    // words in input stream of \n delimited text (eg. generated by 'nc')
    // Note that no duplication in storage level only for running locally.
    // Replication necessary in distributed scenario for fault tolerance.
    val observation = ssc.socketTextStream(args(0), args(1).toInt, StorageLevel.MEMORY_AND_DISK_SER)
    observation.print

    //perhaps I should sample between train and predict
    val trainStream= observation.map(_.split("\t")).filter(x => (x.size >2 && x(0).forall(_.isDigit))).map(x =>
      Rating(x(0).toLong, x(1).toLong, x(2).toFloat)
    )

    //would like to be able to re-train the model periodically but for whatever reason, this doesn't work
    val filteredTrainStream = trainStream.foreachRDD(rdd => {
      if (rdd.count() > 0){
        //algorithm.trainOn(rdd)
      }
    })

    //would like to be able to re-train the model periodically but for whatever reason, this doesn't work
    //algorithm.trainOn(trainStream)

    val testStream= observation.map(_.split("::")).filter(x => {
      (x.size >2 && x(0).forall(_.isDigit))
    }).map(x =>
        (x(0).toLong, x(1).toLong)
    )

    //now predict against the live stream
    //this gives us predicted ratings for the item user combination fed from the stream
    val predictions: DStream[Rating[Long]] = algorithm.predictOn(testStream).cache()
    predictions.map(row => {
      Predictions(row.user.toInt, row.item.toInt, row.rating.toFloat)
    }
    ).foreachRDD(rdd => {
      print("COUNT: " + rdd.count())
      if (rdd.count() > 0) {
        rdd.toString()
        print("going to save to cassandra")
        rdd.saveToCassandra("recommendations", "predictions", SomeColumns("user", "item", "prediction"))
      }
      })

    predictions.map(x => "item: " + x.item + " user: " +  x.user+ " rating: " + x.rating).print()

    //CACHE TABLE table recommendations.predictions once per window (which is once per RDD)
    observation.foreachRDD(row => {
      //hiveContext.sql("select * from recommendations.predictions").persist()
      //hiveContext.cacheTable("recommendations.predictions")
      hiveContext.sql("use recommendations").collect()
      hiveContext.sql("cache table predictions").collect()
      print(s"Cached the predictions table")
      true
    })

    ssc.start()
    ssc.awaitTermination()
  }

  var conf: SparkConf = _
  var sc: SparkContext = _
}

case class Predictions(user: Int, item: Int, prediction: Float)

// scalastyle:on println