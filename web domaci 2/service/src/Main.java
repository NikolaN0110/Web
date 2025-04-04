import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static final int PORT = 9001;


    public static void main(String[] args) {

           Socket socket = null;
           BufferedReader in = null;
           PrintWriter out = null;

            try {
                socket = new Socket("localhost", PORT);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

                    System.out.println(in.readLine()); // Request for username
                    String username = userInput.readLine();
                    out.println(username);

                    System.out.println(in.readLine()); // Welcome message

                BufferedReader finalIn = in;
                new Thread(() -> {
                        try {
                            String serverMessage;
                            while ((serverMessage = finalIn.readLine()) != null) {
                                System.out.println(serverMessage);
                            }
                        } catch (IOException e) {
                            System.err.println("Disconnected from server.");
                        }
                    }).start();

                    String userMessage;
                    while ((userMessage = userInput.readLine()) != null) {
                        out.println(userMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Could not connect to server.");
                }
            }
}
