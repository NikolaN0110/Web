package http.helper;

import com.google.gson.Gson;
import serialization.Quote;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HelpServiceThread implements Runnable {

    private final Socket socket;

    private final Quote quote;



    public HelpServiceThread(Socket socket,Quote quotes) {
        this.socket = socket;
        this.quote = quotes;
    }

    @Override
    public void run() {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true)) {

            String line;
            String path = null;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("GET")) {
                    String[] parts = line.split(" ");
                    path = parts[1];
                }
            }

            if ("/quote".equals(path)) {
                String json = new Gson().toJson(quote);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println("Content-Length: " + json.length());
                out.println();
                out.println(json);
            } else {
                out.println("HTTP/1.1 404 Not Found");
                out.println();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
