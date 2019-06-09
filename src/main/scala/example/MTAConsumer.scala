package example

import org.apache.kafka.clients.consumer.KafkaConsumer
import com.google.transit.realtime.gtfs_realtime._

import java.io.File
import java.time._
import java.util.Properties

import com.github.tototoshi.csv._

import org.json4s._
import org.json4s.native.JsonMethods._

import scala.collection.JavaConverters._
import scala.collection.mutable
import com.google.transit.realtime.nyct_subway.NyctStopTimeUpdate

object MTAConsumer extends App {
 
 class MTATrainTracker(topic: String) {

    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("enable.auto.commit", "true")
    // props.put("auto.commit.interval.ms", "1000")
    props.put("auto.offset.reset", "latest")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer")
    props.put("group.id", "uniquegroup")

    val consumer = new KafkaConsumer[String, Array[Byte]](props)
    consumer.subscribe(java.util.Arrays.asList(topic))

    var stations = mutable.Map[String, String]()

    val filename = "/Users/john/Desktop/master/nyc-protobuf/mta_stations.csv"
    val reader = CSVReader.open(new File(filename))
    reader.readNext()
    reader.foreach(fields => stations += (fields(2).toString -> fields(5).toString))
    reader.close()

    // arrivalTimes += (routeId -> mutable.Map((stopId, direction) -> -1))
    var arrivalTimes = mutable.Map[String, mutable.Map[(String, String), Long]]()

    def processMessage() = {
        val records = consumer.poll(1000)

        for (record <- records.asScala) {

                    val entity: FeedEntity = FeedEntity.parseFrom(record.value)
        
    
                    val tripUpdate: TripUpdate = entity.getTripUpdate
                    val tripDescriptor: TripDescriptor = tripUpdate.trip
                    val routeId: String = tripDescriptor.getRouteId
                    val stopTimeUpdates: Seq[TripUpdate.StopTimeUpdate] = tripUpdate.stopTimeUpdate
                    

                    for (update <- stopTimeUpdates) {
                        val stopId: String = update.getStopId.slice(0, 3)
                        val direction: String = update.getStopId.apply(3).toString

                        try {
                            val newArrival: Long = update.getArrival.getTime
                            val nextArrival: Long = arrivalTimes.getOrElseUpdate(routeId, mutable.Map(("-1", "-1") -> -1))
                                                                .getOrElseUpdate((stopId, direction), -1)

                            val currentTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of("America/New_York"))
                            val now: Long = currentTime.toInstant().getEpochSecond()

                            if ( (newArrival >= now) && (newArrival < nextArrival || nextArrival == -1) ) {
                                arrivalTimes.get(routeId).get((stopId, direction)) = newArrival    
                                val minutes: Int = BigDecimal((newArrival - now) / 60).setScale(0, BigDecimal.RoundingMode.HALF_UP).toInt                              
                                
                                println(s"Next ${direction} bound ${routeId} train will arrive at station ${stations.get(stopId).get} in ${minutes} minutes")
                
                            }

                        }
                        catch {
                            case e: NoSuchElementException => {}
                        }
                        
                    }
                
        
        }
        
    }

    def run() = {
        while(true) {
            this.processMessage()
            Thread.sleep(10000)
        }
    }
 }

 val mta = new MTATrainTracker(topic="test")
 mta.run()

    
}