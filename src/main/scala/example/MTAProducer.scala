package example

import java.util.Properties
import org.apache.kafka.clients.producer._
import com.google.transit.realtime.gtfs_realtime._
import java.net.URL

object MTAProducer extends App {
 
    class MTARealTime(apiKey: String, topic: String) {
    
        val url: URL = new URL(s"http://datamine.mta.info/mta_esi.php?key=${apiKey}&feed_id=1")

        val configs = new Properties()
        configs.put("bootstrap.servers", "localhost:9092")
        configs.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        configs.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")
        val producer = new KafkaProducer[String, Array[Byte]](configs)

        def produceTripUpdates() = {
            try {
                val feed: FeedMessage = FeedMessage.parseFrom(url.openStream())
                // byte-array to proto objects
                for (entity: FeedEntity <- feed.entity) {   
                    if (entity.tripUpdate.isDefined) {
                        val record = new ProducerRecord(topic, "key", entity.toByteArray)
                        producer.send(record)
                        println(record)
                    }
                }      
            }
            catch {
                case e: com.google.protobuf.InvalidProtocolBufferException => e.printStackTrace
            }
        }

        def run() = {
            while (true) {
                this.produceTripUpdates()
                Thread.sleep(10000)
            }
        }
    }
    
    val mta = new MTARealTime(apiKey="xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", topic="test")
    mta.run()
  
    
   
   

}