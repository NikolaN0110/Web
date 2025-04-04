package http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static final int TCP_PORT = 8080;

    public static void main(String[] args) {

        try {
            ServerSocket ss = new ServerSocket(TCP_PORT);
            while (true) {
                Socket sock = ss.accept();
                ServerThread serverThread = new ServerThread(sock);
                new Thread(serverThread).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
