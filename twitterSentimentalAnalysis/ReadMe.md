Generate jar file or take it below s3 link provided

https://bigdataanassignment3.s3.amazonaws.com/Assignment3/Question1/twitterSentimentalAnalysis-assembly-0.1.jar


zookeeper :
   bin\windows\zookeeper-server-start.bat config/zookeeper.properties
   
kafka:
   bin\windows\kafka-server-start.bat config\server.properties


Create topic:
   bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic sentimentanalysis
   
   
  
producer:

bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic sentimentanalyze  
   
consumer:
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic sentimentanalyze --from-beginning


spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.0 --class TwitterMain PathToJar sentimentanalysis window_time_in_seconds filter(can be multiple words seperated by space)
	ex: spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.0 --class TwitterMain PathToJar sentimentanalysis 20 covid trump rgv
	
to start Elastic Search:
./elasticsearch

to start Kibana:
./kibana

start Logstash:
bin/logstash -f logstash-simple.conf

sample logstash-simple.conf file is as follows:

input {
kafka {
bootstrap_servers => "localhost:9092"
topics => ["sentimentanalysis"]
}
}

output {
elasticsearch {
hosts => ["localhost:9200"]
index => "sentimentanalysis-index"
}
}

first take jar file then spark-submit according to above command given.