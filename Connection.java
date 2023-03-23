import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

public class Connection extends User implements Runnable {
//    private User client;
    private Socket socket;
    private Scanner input;
    private PrintWriter output;
    private final Server server;
//    private Timer heartbeatTimer;
    public Object list;

    public Connection(int id, Socket socket, Server server) throws IOException {
        super(id, "", socket.getRemoteSocketAddress().toString());

        this.server = server;
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream(), true);

        //I assume this method is blocking so we will wait until we have a name before continuing (not necessarily ideal for the connection initiliastion).
        super.name = receiveName();

//        this.heartbeatTimer = new Timer();
    }
    public void run() {
        String newLine;
		try {

			output.println("NAMEACCEPTED " + name);

			server.broadcastJoined(getId());

            // Accept messages from this client and broadcast them.
            while (true) {
                newLine = this.input.nextLine();
                if (newLine.toLowerCase().startsWith("/quit")) {
                    return;
                } else if (newLine.startsWith("/msg")) {
                    //this block will send a private message if it is prefixed with "/msg" and contains a user id and then message body.
                    String[] parts = newLine.split(" ", 3);
                   // if (parts.length != 3) //If the message does not contain 3 parts, i.e. prefix, id and body, continue to the next loop iteration.
                        //continue;
                    //TODO: Send ID not name.
                    String receiver = parts[1];
                    String message = parts[2];
                    server.privateMessage("[Private message from " + super.getName() + "]: " + message, Integer.parseInt(receiver));
                } else {
                    //fall back to broadcasting a message.
                    server.broadcast(name + ": " + newLine);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try { server.broadcastLeft(getId()); } catch (IOException e) {}
            server.removeConnection(super.getId());
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
    // public String receiveName() throws IOException {
    private String receiveName() throws IOException {
    	//Keep requesting a name until we get a valid one.
        while (true) {
        	output.println("SUBMITNAME");
        	String username = input.nextLine();

            if (username == null || username.equals(""))
                continue;

            return username;
        }
    }
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
