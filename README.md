# NYC-Subway Tracker
This project is heavily inspired by [Building a real time NYC subway tracker with Apache Kafka](https://hackernoon.com/building-a-real-time-nyc-subway-tracker-with-apache-kafka-40d4e09bfe98).

The MTA NYC-Subway uses [GTFS Realtime](https://developers.google.com/transit/gtfs-realtime/), based on [Protocol Buffers](https://developers.google.com/protocol-buffers/), to track and update the states of the subway train. Thus, we need to generate the Scala class from gtfs-realtime.proto and nyct-subway.proto in order to parsing and serializing the MTA responses.

In this project, [Scalapb](https://scalapb.github.io/) is used as an automatic compiler for the protocol buffer. Simply, just drop 
gtfs-realtime.proto and nyct-subway.proto in the src/main/protobuf and Scalapb will automatically generate Scala case classes, parsers and serializers during compile time.

_Note: the project was bootstraped with `sbt new scala/scala-seed.g8`_

## Prerequisites
1. Request a new API key from [MTA](http://datamine.mta.info/feed-documentation) and replace it in MTAProducer.scala
2. Change mta_stations.csv file location in MTAConsumer.scala

## Getting Started

1.Start Zookeeper and Kafka server with command
```
$ bin/zookeeper-server-start.sh config/zookeeper.properties

$ bin/kafka-server-start.sh config/server.properties
```

2.Create a topic 

```
$ bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test
```
3.Verify that topic 

```
$ bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

4.Build a fat Jar with `sbt assembly`

5.Start the Producer and Consumer:
```
$ java -cp target/nyc-protobuf-assembly-0.1.0-SNAPSHOT.jar example.MTAProducer

$ java -cp target/nyc-protobuf-assembly-0.1.0-SNAPSHOT.jar example.MTAConsumer
```

The output should look like this:

```
Next N bound 6 train will arrive at station Canal St in 4 minutes
Next N bound 6 train will arrive at station Spring St in 6 minutes
Next N bound 6 train will arrive at station Bleecker St in 7 minutes
Next N bound 6 train will arrive at station Astor Pl in 8 minutes
Next N bound 6 train will arrive at station 14 St - Union Sq in 10 minutes
Next N bound 6 train will arrive at station 23 St in 12 minutes
Next N bound 6 train will arrive at station 28 St in 13 minutes
Next N bound 6 train will arrive at station 33 St in 14 minutes
```

## Some useful commands

Describe current offsets and end offsets

```
$ bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group turn-over --describe
```

Reset current offsets to the earliest offset

```
$ bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group turn-over --topic test --reset-offsets --to-earliest --execute
```

Append delete.topic.enable=true to config/server.properties before executing delete command:

```
$ bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic test
```

Terminate Zookeeper and Kafka:

```
$ ps -ef | grep kafka | grep -v grep | awk '{print $2}'| xargs kill -9

$ ps -ef | grep zookeeper | grep -v grep | awk '{print $2}'| xargs kill -9
```
