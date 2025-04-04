package serialization;

public class Quote {

    private String Author;
    private String Quote;

    public Quote(String quote, String author) {
        Quote = quote;
        Author = author;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getQuote() {
        return Quote;
    }

    public void setQuote(String quote) {
        Quote = quote;
    }
}
