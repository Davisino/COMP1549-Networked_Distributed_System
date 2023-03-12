import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

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
    	String coordinatorCeremony = "You are the new coordinator.";
        String newLine;
		try {
			
			output.println("NAMEACCEPTED " + name);
		
			if (server.getUsers().size() == 1) {
				server.personalBroadcast(name, coordinatorCeremony);
				server.getUser(name).setCoordinator(true);
			}
			
			server.broadcastJoined(name + " has joined");
			
            // Accept messages from this client and broadcast them.
            while (true) {
                
            	newLine = this.input.nextLine();
            	
            	// avoid reading empty messages
            	if (newLine.equals("")) {
            		System.out.println("Ups, you're trying to send an empty message. Please make sure you send something :).");
            		continue;
            	}
            	
            	String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
                if (newLine.toLowerCase().startsWith("/quit")) {
                    return;
                } else if (newLine.startsWith("/msg")) {
                    String[] parts = newLine.split(" ", 3);
                    String receiver = parts[1];
                    String message = parts[2];
                    server.privateBroadcast(message, name, receiver);
                    server.saveMessage(name, new Message(server.getUserId(name), name, receiver, message, timeStamp));

                } else {
                    server.broadcast(name + ": " + newLine);
                    server.saveMessage(name, new Message(server.getUserId(name), name, "public", newLine, timeStamp));
                }
                
            }
        } catch (Exception e) {
            System.out.println(e);
 
        } finally {
            if (output != null) {	
            	// if name == coordinator
            	// remove it and choose a new coordinator if size > 1
                boolean isCoordinator = server.getUser(name).isCoordinator() == (true);
                server.removeConnection(name);
                System.out.println(server.getConnections().size());
                if (isCoordinator == true && server.getConnections().size() >= 1) {
                	// Random assignment of coordinator.
        			int randomIndex = new Random().nextInt(server.getUsers().size());
        			server.getUsers().get(randomIndex).setCoordinator(true);
        			
        			try {
						server.personalBroadcast(server.getUsers().get(randomIndex).getName(), coordinatorCeremony);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
                	// otherwise don't set coordinators users.size == 0 and selection will be done automatically.          
                
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
                    // generates a unique id for the new user.
                    String id = UUID.randomUUID().toString();
                    users.add(new User(id,username, ipAddress, portNumber));
                    break;
                }
            }
        
        }
        name = username;
    	return username;

    }
   

}
