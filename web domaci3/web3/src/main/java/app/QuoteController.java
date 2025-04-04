package app;

import http.Request;
import http.ServerThread;
import http.response.HtmlResponse;
import http.response.RedirectResponse;
import http.response.Response;
import serialization.Quote;
import serialization.Serialization;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

public class QuoteController extends Controller {

    private String q;

    private static final String HTML_TEMPLATE = """
                <!DOCTYPE html>
                       <html>
                       <head>
                       <meta charset="UTF-8">
                       <meta name="viewport" content="width=device-width, initial-scale=1">
                       <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css">
                       <title>Quote Saver</title>
                       </head>
                       <body class="p-4 bg-light">
                       <div class="container">
                       <h1 class="text-center mb-4">Daily Quotes</h1>
                       <form action="/save-quote" method="POST" class="mb-4 p-3 bg-white shadow rounded">
                       <label for="author" class="font-weight-bold">Author:</label>
                       <input id="author" name="author" type="text" class="form-control mb-3" placeholder="Enter author">
                       <label for="quote" class="font-weight-bold">Quote:</label>
                       <input id="quote" name="quote" type="text" class="form-control mb-3" placeholder="Enter quote">
                       <button class="btn btn-success w-100">Save Quote</button>
                       </form>
                       <h2 class="mb-3">Quote of the day:</h2>
                       <p class="p-3 bg-white shadow rounded"><b>%s</b> - <i>"%s"</i></p>
                       <h2 class="mt-4 mb-3">Saved Quotes:</h2>
                       <table class="table table-bordered bg-white shadow rounded">%s</table>
                       </div>
            
                       </body>
                       </html>
            """;

    public QuoteController(Request request) {
        super(request);
    }

    @Override
    public Response doGet() {
        String json = fetchQuoteFromHelper();
        Quote dailyQuote = Serialization.getQuote(json);

        List<Quote> quotes = Repository.getInstance().getQuotes();

        String allQuotes = quotes.stream()
                .map(q -> String.format("<tr><td><b>%s</b> - \"%s\"</td></tr>\n", q.getAuthor(), q.getQuote()))
                .collect(Collectors.joining());

        String body = String.format(HTML_TEMPLATE, dailyQuote.getAuthor(), dailyQuote.getQuote(), allQuotes);
        return new HtmlResponse(body);
    }

    @Override
    public Response doPost() {
        String body = request.getBody();
        String[] parts = body.split("&");

        String author = "";
        String quote = "";
        for (String part : parts) {
            String[] keyVal = part.split("=");
            if (keyVal.length == 2) {
                String key = keyVal[0];
                String val = java.net.URLDecoder.decode(keyVal[1]);
                if (key.equals("author")) author = val;
                else if (key.equals("quote")) quote = val;
            }
        }

        if (!author.isBlank() && !quote.isBlank()) {
            Repository.getInstance().addQuote(new Quote(quote, author));
        }

        return new RedirectResponse("/quotes");
    }

    private String fetchQuoteFromHelper() {
        try (Socket socket = new Socket("localhost", 8081);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("GET /quote HTTP/1.1");
            out.println("Host: localhost");
            out.println();

            String line;
            boolean bodyStarted = false;
            StringBuilder json = new StringBuilder();
            while ((line = in.readLine()) != null) {
                if (bodyStarted) {
                    json.append(line);
                }
                if (line.isEmpty()) {
                    bodyStarted = true;
                }
            }

            return json.toString().trim();

        } catch (Exception e) {
            return "{\"quote\": \"Could not fetch quote of the day\", \"author\": \"System\"}";
        }
    }
}



