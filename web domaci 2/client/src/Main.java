import javax.imageio.IIOException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Main {

    public static final int PORT = 9001;
    public static final List<Socket> clientSockets = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (true) {
            System.out.println("Waiting for connection...");
            Socket socket= serverSocket.accept();
            clientSockets.add(socket);
            System.out.println("Connection accepted");
            Thread serverThread = new Thread(new ServerThread(socket));
            serverThread.start();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}