
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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
    private static List<User> users;
    
    private static Map<String, Connection> connections = new HashMap<>();
    
    public static void main(String[] args) throws IOException {
        users = new ArrayList<>();
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
                Connection connection = new Connection(socket, server);
               
                String name = connection.receiveName();
                
                server.addConnection(name, connection);
    
                pool.execute(connection);
            }
        }
       
    }

    public void addConnection(String name, Connection connection) {
    	connections.put(name, connection);
    }
    public void removeConnection(String name) {
    	connections.remove(name);
    }
    
    public List<User> getUsers() {
    	return users;
    }
    public void broadcast(String message) throws IOException {
    	for (Connection connection: connections.values()) {
    		connection.sendMessage(message);
    	}
    }
    
    public void broadcastJoined(String message) throws IOException {
    	for (Connection connection: connections.values()) {
    		connection.hasJoined(message);
    	}
    }
    public void broadcastLeft(String message) throws IOException {
    	for (Connection connection: connections.values()) {
    		connection.hasLeft(message);
 
    	}
    }
    
    public void privateBroadcast(String message, String sender, String receiver) throws IOException{
        Connection senderConnection = connections.get(sender);
        Connection receiverConnection = connections.get(receiver);
   
    
        senderConnection.sendPrivateMessage("To >> "+ receiver + " >> " + message);
        receiverConnection.sendPrivateMessage("From >> " + sender + " >> " + message);

    }
    public Map<String, Connection> getConnections() {
    	return connections;
    }
}
