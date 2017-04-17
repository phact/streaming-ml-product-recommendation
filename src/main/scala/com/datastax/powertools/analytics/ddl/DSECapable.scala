package com.datastax.powertools.analytics.ddl

import com.datastax.spark.connector.CassandraRow
import com.datastax.spark.connector.rdd.CassandraRDD
import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by sebastianestevez on 4/17/17.
  */
trait DSECapable {

  var conf:SparkConf
  var sc: SparkContext

  def connectToDSE(name: String): SparkContext = {
    conf = new SparkConf().setAppName(name)
    sc = SparkContext.getOrCreate(conf)
    sc
  }

  def setupSchema(keyspaceName: String, tableName: String, fields: String): Unit ={
    val connector = CassandraConnector(conf)
    connector.withSessionDo(session => {
      session.execute(s"create keyspace if not exists ${keyspaceName} with replication = { 'class':'SimpleStrategy', " +
        "'replication_factor':1}")
      session.execute(s"create table if not exists ${keyspaceName}.${tableName} " +
        s"${fields}")
    })
    val rdd = sc.cassandraTable(keyspaceName, tableName)
    new CassandraContext(connector, rdd, sc)
  }

}

class CassandraContext(val connector: CassandraConnector,
                       val rdd: CassandraRDD[CassandraRow],
                       val sparkContext: SparkContext)
