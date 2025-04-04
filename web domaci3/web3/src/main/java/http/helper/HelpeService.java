package http.helper;

import serialization.Quote;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class HelpeService {

    private static final int HELPER_SERVER_PORT=8081;
    private static final List<Quote> quotes = Arrays.asList(
            new Quote("The only way to do great work is to love what you do.", "Steve Jobs"),
            new Quote("Success is not final, failure is not fatal: It is the courage to continue that counts.", "Winston Churchill"),
            new Quote("It is never too late to be what you might have been.", "George Eliot"),
            new Quote("The purpose of life is not to be happy. It is to be useful, to be honorable, to be compassionate, to have it make some difference that you have lived and lived well.", "Ralph Waldo Emerson"),
            new Quote("In the end, we will remember not the words of our enemies, but the silence of our friends.", "Martin Luther King Jr.")
    );

    public static Quote getRandomQuote() {
        Random random = new Random();
        return quotes.get(random.nextInt(quotes.size()));
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(HELPER_SERVER_PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new HelpServiceThread(socket,getRandomQuote())).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
