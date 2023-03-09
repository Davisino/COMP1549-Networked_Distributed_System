import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Connection implements Runnable {
//    private User client;
    private Socket socket;
    private Scanner input;
    private PrintWriter output;
//    private Timer heartbeatTimer;
	private Server server;
	private String name;
	
    public Connection(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.input = new Scanner(socket.getInputStream());
        this.output = new PrintWriter(socket.getOutputStream(), true);
        
//        this.heartbeatTimer = new Timer();
    }
    public void run() {
        String newLine;
		try {
			
			output.println("NAMEACCEPTED " + name);
		
			server.broadcastJoined(name + " has joined");
			
            // Accept messages from this client and broadcast them.
            while (true) {
                newLine = this.input.nextLine();
                if (newLine.toLowerCase().startsWith("/quit")) {
                    return;
                } else if (newLine.startsWith("/msg")) {
                    String[] parts = newLine.split(" ", 3);
                    String receiver = parts[1];
                    String message = parts[2];
                    server.privateBroadcast(message, name, receiver);
         
                } else {
                    server.broadcast(name + ": " + newLine);
                 
                }
                
            }
        } catch (Exception e) {
            System.out.println(e);
 
        } finally {
            if (output != null) {	
                server.removeConnection(name);
            }
            if (name != null) {
                System.out.println(name + " is leaving");
                List<User> users = server.getUsers();
                synchronized (users) {
                    users.removeIf(user -> user.getName().equals(name));
                }
                try {
					server.broadcastLeft(name + " has left");
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
            try { socket.close(); } catch (IOException e) {}
        }
    }


    public void sendMessage(String message) throws IOException {
    	// Send message to everyone 
    	output.println("MESSAGE " + message);

    }
    public void hasJoined(String  message) throws IOException {
    	// let everyone knows someone has joined
    	output.println("MESSAGE " + message);
    }
    public void hasLeft(String message) throws IOException {
    	// let everyone knows someone has left
    	output.println("MESSAGE " + message);
    }
    public void sendPrivateMessage(String message) throws IOException {
    	// Send private message to someone
    	output.println("PRIVATE " + message);
    }

//    public void startHeartbeat() {
//        heartbeatTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    sendMessage("HEARTBEAT");
//                } catch (IOException e) {
//                    System.out.println("Error sending heartbeat: " + e.getMessage());
//                }
//            }
//        }, 0, 5000); 
//    }
//
//    public void stopHeartbeat() {
//        heartbeatTimer.cancel();
//    }
    public String receiveName() throws IOException {
        
    	String username;
    	// Keep requesting a name until we get a unique one.
        while (true) {
        	output.println("SUBMITNAME");
        	username = input.nextLine();
            
            if (username == null || username == "") {
                continue;
            }
            List<User> users = server.getUsers();
            synchronized (users) {

                if (!username.isEmpty()) {
                    String ipAddress = socket.getInetAddress().getHostAddress();     
                    int portNumber = socket.getPort();
                    users.add(new User(username, ipAddress, portNumber));
                    break;
                }
            }
        
        }
        name = username;
    	return username;

    }
   

}
