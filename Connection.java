package cw;
import java.util.concurrent.ThreadLocalRandom;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Connection extends User implements Runnable {
    private Socket socket;
    private Scanner input;
    private PrintWriter output;
    private final Server server;
    public Object list;

    public Connection(int id, Socket socket, Server server) throws IOException {
        super(id, "", socket.getRemoteSocketAddress().toString());
        this.server = server;
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream(), true);
        super.name = receiveName();
    }
    public void run() {
        String newLine;
		try {
			output.println("NAMEACCEPTED " + name);	
			server.broadcastJoined(getId());
			if (server.getConnection().size() == 1) {
				server.personalBroadcast(getId(), "You are the coordinator");
				setCoordinator(true);
			}
            // Accept messages from this client and broadcast them.
            while (true) {
                newLine = this.input.nextLine();
                
                String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
                if (newLine.toLowerCase().startsWith("/quit")) {
                    return;
                } else if (newLine.startsWith("/msg")) {
                    //this block will send a private message if it is prefixed with "/msg" and contains a user id and then message body.
                    String[] parts = newLine.split(" ", 3);
                    String receiver = parts[1];
                    String message = parts[2];
                    server.privateMessage(" [" + timeStamp + " ] -> " + "Private message from " + super.getName() + ">>>  " + message, Integer.parseInt(receiver));
                } else if (newLine.startsWith("/details")) {
                	String[] parts = newLine.split(" ", 2);
                	String body = parts[1];
                
                	server.personalBroadcast(getId(), body);
                } else {
                    server.broadcast(" [" + timeStamp + "] -> " + name + ">>>: " + newLine );
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try { server.broadcastLeft(getId()); } catch (IOException e) {}
            
            // SELECT RANDOM COORDINATOR LOGIC.
            
            if (isCoordinator() == true && server.getConnection().size() > 1) {
            	
            	// Remove Current Client from List of Clients to avoid choosing same Client.
            	server.removeConnection(super.getId());
            	
            	List<Integer> iDs = new ArrayList<>();
            	
            	// Create a List of all Coordinators IDs we can choose from.
            	for (Connection connection: server.getConnection().values()) {
            		iDs.add(connection.getId());
                }
            	
            	// Choose a random index
            	int randomNum = ThreadLocalRandom.current().nextInt(0, iDs.size() + 1);
            	
            	// 
            	Integer newCoordinatorId = iDs.get(randomNum);
            	
            	// Select new Client and set coordinator to TRUE.
            	server.getConnection().get(newCoordinatorId).setCoordinator(true);
            	
            	try {
					server.personalBroadcast(newCoordinatorId, "*!! You are the new coordinator !!");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
            } else {
            	server.removeConnection(super.getId());
            }
            
            if (name != null) {
                System.out.println(name + " is leaving");
            }
            try
            {
                if (socket != null)
                    socket.close();
            }
            catch (IOException e) {}
        }
    }


    public void sendMessage(String message) throws IOException {
    	// Send message to everyone
    	output.println("MESSAGE " + message);

    }
    public void hasJoined(Integer id) throws IOException {
    	// let everyone knows someone has joined
        sendStatusUpdate(id, true);
    }
    public void hasLeft(Integer id) throws IOException {
    	// let everyone knows someone has left
        sendStatusUpdate(id, false);
    }
    private void sendStatusUpdate(Integer id, boolean connected)
    {
        output.println("USER " + seralizeUser(id) + ":" + connected);
    }
    public void sendPrivateMessage(String message) throws IOException {
    	// Send private message to someone
    	output.println("PRIVATE " + message);
    }

    // This function sends a signal to the client to ask a name from the user
    private String receiveName() throws IOException {
        while (true) {
        	output.println("SUBMITNAME");
        	String username = input.nextLine();

            if (username == null || username.equals(""))
                continue;

            return username;
        }
    }
    // This function is called every time a new user joins to be able to send the new user
    // all the users that are connected and the user be able to contact them.
    public void SendUsers()
    {
        String data = "USERS ";
        for (Map.Entry<Integer, Connection> entry : server.connections.entrySet())
            data += seralizeUser(entry.getKey()) + ",";
        if (data.endsWith(","))
            data = data.substring(0, data.length() - 1);
        output.println(data);
    }
    
    private String seralizeUser(Integer id)
    {
        Connection connection = server.connections.get(id);
        return connection.getId()
            + ":" + connection.getName()
            + ":" + connection.getAddress();
    }
}
