package serialization;

import com.google.gson.Gson;

public class Serialization {
    public static Quote getQuote(String json) {

        Gson gson = new Gson();
        Quote quote = gson.fromJson(json, Quote.class);

        return quote;

    }

    public static String toJson(Quote quote) {
        return new Gson().toJson(quote);
    }
}
