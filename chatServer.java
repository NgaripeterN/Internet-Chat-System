import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class chatServer {
    private static final int PORT = 23569;
    private static Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat Server is running on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void broadcastMessage(String message) {
        System.out.println("Broadcasting: " + message); // Debug print
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    static void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String nickname;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("Enter your nickname:");
                nickname = in.readLine();
                if (nickname == null || nickname.trim().isEmpty()) {
                    nickname = "Anonymous";
                }
                broadcastMessage(nickname + " has joined the chat");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("/quit")) {
                        break;
                    }
                    broadcastMessage(nickname + ": " + message);
                }
            } catch (IOException e) {
                System.out.println(nickname + " disconnected unexpectedly.");
            } finally {
                closeConnection();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        private void closeConnection() {
            removeClient(this);
            broadcastMessage(nickname + " has left the chat");
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
