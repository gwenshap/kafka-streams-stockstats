# kafka-streams-stockstats
Kafka Streams Example - Some aggregates on fake stock data

This example reads fake "trades" from topic 'stocks'. Each trade includes a type ('ASK' or 'BID'), the stock ticker identifier, the ask price and number of stocks they offer to buy.
For simplicity we only include 'ASK' types and only 10 fake stocks. Every second we output statistics on the previous 5 second window - number of trades, minimum price, avg price and total price.

In next iteration we'll also add the 3 stocks with lowest price every 10 seconds.

## To run:
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
`$ java -cp target/uber-kafka-streams-stockstats-1.1-SNAPSHOT.jar -DLOGLEVEL=INFO com.shapira.examples.streams.stockstats.StockGenProducer <config file>`

4. Run the streams app:
`java -cp target/uber-kafka-streams-stockstats-1.1-SNAPSHOT.jar -DLOGLEVEL=INFO com.shapira.examples.streams.stockstats.StockStatsExample <config file>`

5. Check the results:

   `ccloud consume -b -t stockstats-output`

   or

   `bin/kafka-console-consumer.sh --topic stockstats-output --from-beginning --bootstrap-server localhost:9092  --property print.key=true`

## If you want to reset state and re-run the application (maybe with some changes?) on existing input topic, you can:

1. Reset internal topics (used for shuffle and state-stores):

    `bin/kafka-streams-application-reset.sh --application-id stockstat --bootstrap-servers localhost:9092 --input-topic stocks`

2. (optional) Delete the output topic:

    `bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic stockstats-output`
    
    
## If you want to build a Docker image and publish to Google's private docker registry:
    
0. Make sure Docker is running and edit the pom.xml file to point to the docker repository of your choice (currently it is Google's).

1. Build an image and push to the docker registry: `mvn compile jib:build`
or you can build to a local tar: `mvn compile jib:buildTar`

2. Run the image: `docker run -ti -e "JAVA_TOOL_OPTIONS=-DLOGLEVEL=INFO" --rm gcr.io/gwen-test-202722/kafka-streams-stockstat:latest`

## If you want to run on Kubernetes (GKE example)

1. Create GKE cluster:
`gcloud container clusters create kafka-streams-cluster \
     --num-nodes 2 \
     --machine-type n1-standard-1 \
     --zone us-central1-c`
     
2. Deploy the container (stateless) - one instance, as specified in deployment:
`kubectl create -f kafka-streams-stockstats-deployment.yaml`

   you can start another window to watch the logs:
``kubectl logs `kubectl get pods -l app=streams-stock-stats -o=name` -f``

   and you can watch the output (with timestamps!):
`ccloud consume -t stockstats-output | ruby -pe 'print Time.now.strftime("[%Y-%m-%d %H:%M:%S] ")'`

3. Now scale up to 3 instances (more is pointless since we only have 3 partitions):
`kubectl scale deployment streams-stock-stats --replicas=3`

  Since we only configured 2 nodes and our deployment has "Anti-Affinity" properties, only 2 of the 3 instances will be scheduled (one on each node) and one will be pending. You can see that by running:
  
  `kubectl get pods`

   Watch the logs for the rebalance and the output to see that the job just keeps running!

4. And scale back down:
`kubectl scale deployment streams-stock-stats --replicas=1`

5. Finally, you can just kill the whole job:
`kubectl delete -f kafka-streams-stockstats-deployment.yaml`

## If you want to run with a stateful set!

This example has a tiny tiny state, so if we restart a pod and the local state is lost and needs to be re-created, no big deal. But if you have large state, you'll want to preserve it between restarts. Note that while I configured shared storage, I didn't worry about stateful network identity - since this example doesn't include interactive queries.

1. You can watch the pods getting created, note how they each have an identity:
`kubectl get pods -w -l app=streams-stock-stats`

2. Start the stateful set: `kubectl create -f kafka-streams-stockstats-stateful.yaml`

3. Delete a pod and watch it restart with its old state:
`kubectl delete pods streams-stock-stats-1`

4. And finally, we can get rid of the entire set. Note that the storage will remain:
`kubectl delete -f kafka-streams-stockstats-stateful.yaml`
