import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}
import org.apache.spark.streaming.twitter.TwitterUtils
import twitter4j.conf.ConfigurationBuilder
import twitter4j.auth.OAuthAuthorization
import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}


object TwitterMain {
  def main(args: Array[String]) {
    if (args.length<2) {
     System.err.println("Input Arguments <KafkaTopic> window length Filter")
    System.exit(1)
    }

    val topic = args(0).toString
    val filters = args.slice(2, args.length)
    val windowLength=args(1).toInt

    if (!Logger.getRootLogger.getAllAppenders.hasMoreElements) {
      Logger.getRootLogger.setLevel(Level.WARN)
    }



    val sparkConf = new SparkConf().setAppName("TwitterMain")


    if (!sparkConf.contains("spark.master")) {
      sparkConf.setMaster("local[2]")
    }

    val ssc = new StreamingContext(sparkConf, Seconds(windowLength))


    val cb = new ConfigurationBuilder
    cb.setDebugEnabled(true).setOAuthConsumerKey("")
      .setOAuthConsumerSecret("")
      .setOAuthAccessToken("")
      .setOAuthAccessTokenSecret("")
    val auth = new OAuthAuthorization(cb.build)

    val stream = TwitterUtils.createStream(ssc, Some(auth),filters)
    val englishTweets = stream.filter(_.getLang() == "en")

    val hashTags = englishTweets.map(status => status.getText)
    .map(sentence=>(SentimentPredicter.mainSentiment(sentence)))
    hashTags.saveAsTextFiles("data/test/tweets", "json")

    hashTags.foreachRDD { (rdd, time) =>

      rdd.foreachPartition { partitionIter =>

        val props = new Properties()
        val bootstrap = "localhost:9092"
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("bootstrap.servers", bootstrap)
        val producer = new KafkaProducer[String, String](props)
        partitionIter.foreach { elem =>
          val dat = elem.toString()
          val data = new ProducerRecord[String, String](topic, null, dat)
          producer.send(data)
        }
        producer.flush()
        producer.close()
      }
    }
    ssc.start()
    ssc.awaitTermination()
  }
}
