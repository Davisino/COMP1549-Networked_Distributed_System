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
            
            // Now that a successful name has been chosen, add the socket's print writer
            // to the set of all writers so this client can receive broadcast messages.
            // But BEFORE THAT, let everyone else know that the new person has joined!
        
			output.println("NAMEACCEPTED " + name);
			server.broadcast(name + " has joined");
			
            // Accept messages from this client and broadcast them.
            while (true) {
                newLine = readMessage();
                if (newLine.toLowerCase().startsWith("/quit")) {
                    return;
                } else if (newLine.startsWith("/msg")) {
                    String[] parts = newLine.split(" ", 3);
                    String receiver = parts[1];
                    String message = parts[2];
                    
                    server.privateBroadcast(message, name, receiver);
                    
                } else {

                    server.broadcast(newLine);
                    
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
					server.broadcast(name + " has left");
				} catch (IOException e) {
	
					e.printStackTrace();
				}
            }
            try { socket.close(); } catch (IOException e) {}
        }
    }


    public void sendMessage(String message) throws IOException {
    	String[] deter = message.split(" ", 3);


    	if (deter[2].equals("joined")) {
    		System.out.println(deter[1]);
        	System.out.println(deter[2]);
            output.println("MESSAGE " + name + " has joined");
    	} else {
    		output.println("MESSAGE " + name + ": " + message);
    	}
       
    }
    public void sendPrivateMessage(String message) throws IOException {
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
        // Keep requesting a name until we get a unique one.
    	String username;
    	
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
    public PrintWriter getOutput() {
    	return output;
    }
   
    private String readMessage() {
        String message = null;
        message = input.nextLine();
        return message;
    }


}
