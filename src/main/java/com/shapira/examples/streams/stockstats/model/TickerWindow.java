package com.shapira.examples.streams.stockstats.model;

/**
 * Created by gwen on 1/25/17.
 */
public class TickerWindow {
    String ticker;
    long timestamp;

    public TickerWindow(String ticker, long timestamp) {
        this.ticker = ticker;
        this.timestamp = timestamp;
    }
}
