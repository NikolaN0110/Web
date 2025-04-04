package http;

import app.RequestHandler;
import http.response.Response;
import serialization.Quote;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerThread implements Runnable {

    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ServerThread(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String requestLine = in.readLine();
            if (requestLine == null) return;

            StringTokenizer tokenizer = new StringTokenizer(requestLine);
            String method = tokenizer.nextToken();
            String path = tokenizer.nextToken();

            int contentLength = 0;
            String line;
            while (!(line = in.readLine()).isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(" ")[1]);
                }
            }

            String body = "";
            if (method.equals("POST") && contentLength > 0) {
                char[] buffer = new char[contentLength];
                in.read(buffer, 0, contentLength);
                body = new String(buffer);
            }

            Request request = new Request(HttpMethod.valueOf(method), path, body);

            RequestHandler handler = new RequestHandler();
            Response response = handler.handle(request);

            out.println(response.getResponseString());

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}