package app;

import serialization.Quote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Repository {
    private final List<Quote> quotes = Collections.synchronizedList(new ArrayList<>());
    private static Repository instance = null;

    private Repository() {}

    public static Repository getInstance() {
        if (instance == null) {
            instance = new Repository();
        }
        return instance;
    }

    public void addQuote(Quote quote) {
        quotes.add(quote);
    }

    public List<Quote> getQuotes() {
        return new ArrayList<>(quotes);
    }
}

