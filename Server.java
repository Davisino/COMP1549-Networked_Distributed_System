
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

/**
 * A multithreaded chat room server. When a client connects the server requests a screen
 * name by sending the client the text "SUBMITNAME", and keeps requesting a name until
 * a unique one is received. After a client submits a unique name, the server acknowledges
 * with "NAMEACCEPTED". Then all messages from that client will be broadcast to all other
 * clients that have submitted a unique screen name. The broadcast messages are prefixed
 * with "MESSAGE".
 *
 * This is just a teaching example so it can be enhanced in many ways, e.g., better
 * logging. Another is to accept a lot of fun commands, like Slack.
 */
public class Server {
    private static Random random = new Random();

    public static Map<Integer, Connection> connections = new HashMap<>();

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        System.out.println("The chat server is running...");

        String ipAddress = "0.0.0.0"; // replace with your local IP address
        int port = 59001;

        ExecutorService pool = Executors.newFixedThreadPool(500);
        InetSocketAddress address = new InetSocketAddress(ipAddress, port);
        ServerSocket listener = new ServerSocket();
        listener.bind(address);

        try (listener) {
            while (true) {
                Socket socket = listener.accept();

                //Generate Id for the users with random by using random numbers
                int id;
                do { id = random.nextInt(); }
                while(connections.containsKey(id) && id != 0); //0 is the broadcast id.
                Connection connection = new Connection(id, socket, server);

                server.addConnection(id, connection);
                pool.execute(connection);

                connection.SendUsers();
            }
        }
    }

    public void addConnection(Integer id, Connection connection) {
    	connections.put(id, connection);
    }

    public void removeConnection(Integer id) {
    	connections.remove(id);
    }

    public void broadcast(String message) throws IOException {
    	for (Connection connection: connections.values()) {
    		connection.sendMessage(message);
    	}
    }

    public void broadcastJoined(Integer id) throws IOException {
    	for (Connection connection: connections.values()) {
    		connection.hasJoined(id);
    	}
    }

    public void broadcastLeft(Integer id) throws IOException {
    	for (Connection connection: connections.values()) {
            if (connection.getId() != id)
    		    connection.hasLeft(id);
    	}
    }

    public void privateMessage(String message, Integer receiverID) throws IOException {
        for (Connection connection: connections.values()) {
            if (connection.getId() == receiverID)
                connection.sendPrivateMessage(message);
        }
    }
}
