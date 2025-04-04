import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerThread implements Runnable {

    private Socket socket;
    private static final Set<String> activeUsers = ConcurrentHashMap.newKeySet();
    private static final List<String> messageHistory = new CopyOnWriteArrayList<>();
    private static final int HISTORY_LIMIT = 100;
    private static final Set<String> banedWords = Set.of("brod", "avokado");
    private String username;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        BufferedReader in = null;
        PrintWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            out.println("enter username:");
            while (true) {

                username = in.readLine();
                if (username == null || activeUsers.contains(username)) {
                    out.println("This username is already in use");
                } else {
                    synchronized (activeUsers) {
                        activeUsers.add(username);

                    }
                    break;
                }

            }

            out.println("Welcome " + username);
            sendToAllUsers(username," join the chat");
            sendHistory(out);

            String message;
            while ((message = in.readLine()) != null) {
                String filteredMessage = filterMessage(message);
                String formattedMessage = formatMessage(username, filteredMessage);
                synchronized (messageHistory) {
                    if (messageHistory.size() >= HISTORY_LIMIT) {
                        messageHistory.remove(0);
                    }
                    messageHistory.add(formattedMessage);
                }
                sendToAllUsers(username, filteredMessage);
            }



        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (out != null) {
                out.close();
            }

            if (this.socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private void removingUser() {
        try {
            if (username != null) {
                activeUsers.remove(username);
                sendToAllUsers(username + " has left the chat.", "Server");
            }
            Main.clientSockets.remove(socket);
            socket.close();
        } catch (Exception e) {
            System.err.println("Ussername not found: " + username);
        }
    }

    private void sendToAllUsers(String user, String message) {
        String formattedMessage = formatMessage(username, message);
        System.out.println(formattedMessage);

        synchronized (Main.clientSockets) {
            for (Socket client : Main.clientSockets) {
                try {
                    PrintWriter clientOut = new PrintWriter(client.getOutputStream(), true);
                    clientOut.println(formattedMessage);
                } catch (IOException e) {
                    System.err.println("Error broadcasting to clients: " + e.getMessage());
                }
            }
        }
    }

        private String formatMessage (String user, String message){
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            return timestamp + " - " + user + ": " + message;
        }

    private String filterMessage(String message) {
        for (String word : banedWords) {
            message = message.replaceAll("\\b" + word + "\\b", word.charAt(0) + "*".repeat(word.length() - 2) + word.charAt(word.length() - 1));
        }
        return message;
    }

    private void sendHistory(PrintWriter out) {
        out.println("Chat history:");
        for (String msg : messageHistory) {
            out.println(msg);
        }
    }

}
