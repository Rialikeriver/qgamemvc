package Network;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class MP_Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> onMessageReceived;

    public void connect(String ip, int port, Consumer<String> callback) throws IOException {
        this.socket = new Socket(ip, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.onMessageReceived = callback;

        // Start a thread to listen for messages from the server
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    if (onMessageReceived != null) onMessageReceived.accept(line);
                }
            } catch (IOException e) {
                System.out.println("Disconnected from server.");
            }
        }).start();
    }

    public void send(String msg) {
        if (out != null) out.println(msg);
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}
