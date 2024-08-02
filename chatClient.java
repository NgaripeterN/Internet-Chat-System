import java.io.*;
import java.net.*;

public class chatClient {
    private static final int PORT = 23567;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java ChatClient <server_address>");
            return;
        }

        String serverAddress = args[0];

        try (Socket socket = new Socket(serverAddress, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to the chat server at " + serverAddress);
            System.out.println("Type '/quit' to exit the chat");

            // Get nickname from user
            System.out.print("Enter your nickname: ");
            String nickname = stdIn.readLine();
            out.println(nickname);

            // Thread for reading messages from the server
            Thread readerThread = new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from the server.");
                }
            });
            readerThread.start();

            // Main thread for sending messages
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                if (userInput.equalsIgnoreCase("/quit")) {
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("Error connecting to the server: " + e.getMessage());
        }

        System.out.println("Chat session ended.");
    }
}
