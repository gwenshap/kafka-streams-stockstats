package com.shapira.examples.streams.stockstats;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import com.shapira.examples.streams.stockstats.avro.Trade;


import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;


public class StockGenProducer {

    public static KafkaProducer<String, Trade> producer = null;

    public static void main(String[] args) throws Exception {

        System.out.println("Press CTRL-C to stop generating data");


        // add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Shutting Down");
                if (producer != null)
                    producer.close();
            }
        });

        // Configuring producer
        Properties props = new Properties();

        props.put("bootstrap.servers", Constants.BROKER);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
        props.put("schema.registry.url", "http://localhost:8081");

        // Starting producer
        producer = new KafkaProducer<>(props);


        // initialize

        Random random = new Random();
        long iter = 0;

        Map<String, Integer> prices = new HashMap<>();

        for (String ticker : Constants.TICKERS)
            prices.put(ticker, Constants.START_PRICE);

        // Start generating events, stop when CTRL-C

        while (true) {
            iter++;
            for (String ticker : Constants.TICKERS) {
                double log = random.nextGaussian() * 0.25 + 1; // random var from lognormal dist with stddev = 0.25 and mean=1
                int size = random.nextInt(100);
                int price = prices.get(ticker);

                // flunctuate price sometimes
                if (iter % 10 == 0) {
                    price = price + random.nextInt(Constants.MAX_PRICE_CHANGE * 2) - Constants.MAX_PRICE_CHANGE;
                    prices.put(ticker, price);
                }

                Trade trade = new Trade("ASK",ticker,(price+log),size);
                // Note that we are using ticker as the key - so all asks for same stock will be in same partition
                ProducerRecord<String, Trade> record = new ProducerRecord<>(Constants.STOCK_TOPIC, ticker, trade);

                producer.send(record, (RecordMetadata r, Exception e) -> {
                    if (e != null) {
                        System.out.println("Error producing to topic " + r.topic());
                        e.printStackTrace();
                    }
                });

                // Sleep a bit, otherwise it is frying my machine
                Thread.sleep(Constants.DELAY);
            }
        }
    }
}
