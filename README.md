# kafka-streams-stockstats
Kafka Streams Example - Some aggregates on fake stock data

This example reads fake "trades" from topic 'stocks'. Each trade includes a type ('ASK' or 'BID'), the stock ticker identifier, the ask price and number of stocks they offer to buy.
For simplicity we only include 'ASK' types and only 10 fake stocks. Every second we output statistics on the pervious 5 second window - number of trades, minimum price, avg price and total price.

In next iteration we'll also add the 3 stocks with lowest price every 10 seconds.

To run:
--------
0. Build the project with `mvn package`, this will generate an uber-jar with the streams app and all its dependencies.
1. Create a stocks input topic:

    `bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic stocks --partitions 1 --replication-factor 1`

2. Next, we need to generate some trades so we can analyze them. Start running the trades producer and stop it with ctrl-c when you think there's enough data:
`$ java -cp target/uber-kafka-streams-stockstats-1.0-SNAPSHOT.jar com.shapira.examples.streams.stockstats.StockGenProducer`

3. Run the streams app:
`java -cp target/uber-kafka-streams-stockstats-1.0-SNAPSHOT.jar com.shapira.examples.streams.stockstats.StockStatsExample`
Streams apps typically run forever, but this one will just run for a minute and exit

4. Check the results:
`bin/kafka-console-consumer.sh --topic stockstats-output --from-beginning --bootstrap-server localhost:9092  --property print.key=true`

If you want to reset state and re-run the application (maybe with some changes?) on existing input topic, you can:

1. Reset internal topics (used for shuffle and state-stores):

    `bin/kafka-streams-application-reset.sh --application-id stockstat --bootstrap-servers localhost:9092 --input-topic stocks`

2. (optional) Delete the output topic:

    `bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic stockstats-output`
