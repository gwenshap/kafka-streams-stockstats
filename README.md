# kafka-streams-stockstats
Kafka Streams Example - Some aggregates on fake stock data

This example reads fake "trades" from topic 'stocks'. Each trade includes a type ('ASK' or 'BID'), the stock ticker identifier, the ask price and number of stocks they offer to buy.
For simplicity we only include 'ASK' types and only 10 fake stocks. Every second we output statistics on the pervious 5 second window - number of trades, minimum price, avg price and total price.

In next iteration we'll also add the 3 stocks with lowest price every 10 seconds.

To run:
--------
0. Build the project with `mvn package`, this will generate an uber-jar with the streams app and all its dependencies.
1. Create a stocks input topic and output topic:

    ```
    ccloud topic create -t stocks --partitions 3
    
    ccloud topic create -t stockstats-output --partitions 3
    ```
    or

    ```
    bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic stocks --partitions 1 --replication-factor 1`
    
    bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic stockstats-output --partitions 1 --replication-factor 1
   ```

2. We need a configuration file to tell us which brokers to connect to and how to connect to them. If you are a Confluent Cloud user and used ccloud earlier, we are good. Otherwise, create a file with `bootstrap.servers` and any other parameters you need to connect to your brokers (security, etc). You can put other client configation here, but we may override it.

3. Next, we need to generate some trades so we can analyze them. Start running the trades producer and stop it with ctrl-c when you think there's enough data:
`$ java -cp target/uber-kafka-streams-stockstats-1.1-SNAPSHOT.jar com.shapira.examples.streams.stockstats.StockGenProducer <config file>`

4. Run the streams app:
`java -cp target/uber-kafka-streams-stockstats-1.1-SNAPSHOT.jar com.shapira.examples.streams.stockstats.StockStatsExample <config file>`

5. Check the results:

`ccloud consume -b -t stockstats-output`

or

`bin/kafka-console-consumer.sh --topic stockstats-output --from-beginning --bootstrap-server localhost:9092  --property print.key=true`

If you want to reset state and re-run the application (maybe with some changes?) on existing input topic, you can:

1. Reset internal topics (used for shuffle and state-stores):

    `bin/kafka-streams-application-reset.sh --application-id stockstat --bootstrap-servers localhost:9092 --input-topic stocks`

2. (optional) Delete the output topic:

    `bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic stockstats-output`
