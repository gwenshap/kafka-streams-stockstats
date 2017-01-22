package com.shapira.examples.streams.stockstats;

public class Constants {
    public static final String STOCK_TOPIC = "stocks";
    public static final String[] TICKERS = {"MMM", "ABT", "ABBV", "ACN", "ATVI", "AYI", "ADBE", "AAP", "AES", "AET"};
    public static final int MAX_PRICE_CHANGE = 5;
    public static final int START_PRICE = 5000;
    public static final int DELAY = 100; // sleep in ms between sending "asks"
    public static final String BROKER = "localhost:9092";

}
