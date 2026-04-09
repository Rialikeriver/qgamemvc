package Network;

import java.io.*;
import java.net.*;
import java.util.*;

public class MP_Server extends Thread {
    private int port;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private boolean isRunning = false;

    public MP_Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("Server started on port: " + port);

            while (isRunning) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, this);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
        }
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void stopServer() {
        isRunning = false;
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException e) {}
    }

    // Inner class to handle individual client connections
    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private MP_Server server;

        public ClientHandler(Socket socket, MP_Server server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String input;
                while ((input = in.readLine()) != null) {
                    // When a client sends a message (like Chat), broadcast it to everyone
                    server.broadcast(input);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected.");
            } finally {
                clients.remove(this);
            }
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }
    }
}