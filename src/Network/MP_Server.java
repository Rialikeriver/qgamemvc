package Network;

import java.io.*;
import java.net.*;
import java.util.*;

public class MP_Server extends Thread {
    private final int port;
    private ServerSocket serverSocket;

    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private final List<String> serverKnownPlayers = Collections.synchronizedList(new ArrayList<>());

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

    public void broadcast(String msg) {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                c.sendMessage(msg);
            }
        }
    }

    private void broadcastPlayerList() {
        String payload = String.join(",", serverKnownPlayers);
        System.out.println("[SERVER] PLAYER_LIST -> " + payload);

        broadcast(MP_Protocol.format(
                MP_Protocol.PLAYER_LIST,
                "SERVER",
                payload
        ));
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final MP_Server server;

        private PrintWriter out;
        private BufferedReader in;

        private String playerName;

        public ClientHandler(Socket socket, MP_Server server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String input;

                while ((input = in.readLine()) != null) {

                    String[] parts = MP_Protocol.parse(input);
                    String type = parts[0];
                    String sender = parts[1];

                    // JOIN
                    if (MP_Protocol.JOIN.equals(type)) {
                        playerName = sender;

                        if (!serverKnownPlayers.contains(playerName)) {
                            serverKnownPlayers.add(playerName);
                        }

                        broadcastPlayerList();
                    }

                    // REQUEST_PLAYER_LIST
                    if (MP_Protocol.REQUEST_PLAYER_LIST.equals(type)) {
                        String payload = String.join(",", serverKnownPlayers);
                        sendMessage(MP_Protocol.format(
                                MP_Protocol.PLAYER_LIST,
                                "SERVER",
                                payload
                        ));
                        continue;
                    }

                    server.broadcast(input);
                }

            } catch (IOException e) {
                System.out.println("Client disconnected.");
            }

            finally {
                clients.remove(this);

                if (playerName != null && !playerName.isBlank()) {
                    serverKnownPlayers.remove(playerName);

                    server.broadcast(MP_Protocol.format(
                            MP_Protocol.LEAVE,
                            playerName,
                            ""
                    ));

                    broadcastPlayerList();
                }

                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        public void sendMessage(String msg) {
            if (out != null) out.println(msg);
        }
    }
}
