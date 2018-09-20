package com.shapira.examples.streams.stockstats.model;

/**
 * Created by gwen on 1/22/17.
 */
public class Trade {

    String type;
    String ticker;
    double price;
    int size;

    public Trade(String type, String ticker, double price, int size) {
        this.type = type;
        this.ticker = ticker;
        this.price = price;
        this.size = size;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "type='" + type + '\'' +
                ", ticker='" + ticker + '\'' +
                ", price=" + price +
                ", size=" + size +
                '}';
    }
}
