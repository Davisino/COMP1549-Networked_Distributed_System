
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.awt.BorderLayout;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 * A simple Swing-based client for the chat server. Graphically it is a frame with a text
 * field for entering messages and a textarea to see the whole dialog.
 *
 * The client follows the following Chat Protocol. When the server sends "SUBMITNAME" the
 * client replies with the desired screen name. The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are already in use. When the
 * server sends a line beginning with "NAMEACCEPTED" the client is now allowed to start
 * sending the server arbitrary strings to be broadcast to all chatters connected to the
 * server. When the server sends a line beginning with "MESSAGE" then all characters
 * following this string should be displayed in its message area.
 */
public class Client {
    String serverAddress;
    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(50);
    JTextArea messageArea = new JTextArea(16, 50);
    JPanel userListArea = new JPanel();
    ButtonGroup userListButtons = new ButtonGroup();
    Map<Integer, User> users = new HashMap<Integer, User>();

    /**
     * Constructs the client by laying out the GUI and registering a listener with the
     * textfield so that pressing Return in the listener sends the textfield contents
     * to the server. Note however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED message from
     * the server.
     */
    public Client(String serverAddress) {
        this.serverAddress = serverAddress;

        textField.setEditable(false);
        messageArea.setEditable(false);

        frame.setLayout(new GridBagLayout());

        //Message log area.
        GridBagConstraints chatAreaConstraints = new GridBagConstraints();
        chatAreaConstraints.anchor = GridBagConstraints.CENTER;
        chatAreaConstraints.fill = GridBagConstraints.BOTH;
        chatAreaConstraints.gridx = 1;
        chatAreaConstraints.gridy = 0;
        chatAreaConstraints.weightx = 0.7;
        frame.getContentPane().add(new JScrollPane(messageArea), chatAreaConstraints);

        //Message input field.
        GridBagConstraints inputFieldConstraints = new GridBagConstraints();
        inputFieldConstraints.anchor = GridBagConstraints.CENTER;
        inputFieldConstraints.fill = GridBagConstraints.BOTH;
        inputFieldConstraints.gridx = 1;
        inputFieldConstraints.gridy = 1;
        inputFieldConstraints.weightx = 0.7;
        frame.getContentPane().add(textField, inputFieldConstraints);

        //User list.
        GridBagConstraints userListConstraints = new GridBagConstraints();
        userListConstraints.anchor = GridBagConstraints.CENTER;
        userListConstraints.fill = GridBagConstraints.BOTH;
        userListConstraints.gridx = 0;
        userListConstraints.gridy = 0;
        userListConstraints.gridheight = 2;
        userListConstraints.weightx = 0.3;
        AddUserToPanel(new User(0, "All", "0.0.0.0"));
        userListButtons.getElements().nextElement().setSelected(true);
        frame.getContentPane().add(new JScrollPane(userListArea), userListConstraints);

        frame.setSize(600, 400);

        // Send on enter then clear to prepare for next message
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButton selectedButton = null;
                for (Component child : userListArea.getComponents())
                {
                    if (child instanceof JRadioButton)
                    {
                        JRadioButton button = (JRadioButton)child;
                        if (button.isSelected())
                        {
                            selectedButton = button;
                            break;
                        }
                    }
                }
                Integer selectedUserId = (Integer)selectedButton.getClientProperty("user_id");
                if (selectedUserId == 0)
                {
                    //Boradcast.
                    out.println(textField.getText());
                }
                else
                {
                    //Send a private message using the selected user's id.
                    out.println("/msg " + selectedUserId + " " + textField.getText());
                    messageArea.append(textField.getText() + "\n");
                }
                textField.setText("");
            }
        });
    }

    private void AddUserToPanel(User user)
    {
        //Create the user entry.
        JRadioButton userEntry = new JRadioButton();
        userEntry.putClientProperty("user_id", user.getId());
        userEntry.setText(user.name);

        userListButtons.add(userEntry);

        GridBagConstraints userEntryConstraints = new GridBagConstraints();
        userEntryConstraints.gridx = 0;
        userEntryConstraints.gridy = GetUsersUICount();
        userEntryConstraints.fill = GridBagConstraints.HORIZONTAL;
        userEntryConstraints.weightx = 1.0;

        userListArea.add(userEntry, userEntryConstraints);

        UpdateUserPanel();
    }

    private void RemoveUserFromPanel(Integer id)
    {
        for (Component child : userListArea.getComponents())
        {
            if (child instanceof JRadioButton)
            {
                JRadioButton button = (JRadioButton)child;
                if (button.getClientProperty("user_id") == id)
                {
                    userListArea.remove(button);
                    userListButtons.remove(button);
                    UpdateUserPanel();
                }
                else if (button.getClientProperty("user_id") == (Integer)0)
                {
                    button.setSelected(true);
                }
            }
        }
    }

    private int GetUsersUICount()
    {
        int count = 0;
        for (Component child : userListArea.getComponents())
            if (child instanceof JRadioButton)
                count++;
        return count;
    }

    private void UpdateUserPanel()
    {
        //Get the filler.
        Object _filler = userListArea.getClientProperty("filler");
        if (!(_filler instanceof JLabel))
        {
            _filler = new JLabel();
            userListArea.putClientProperty("filler", _filler);
        }
        JLabel filler = (JLabel)_filler;

        //Get the child count.
        int childCount = GetUsersUICount();

        //Remove the old filler.
        userListArea.remove(filler);

        //Update the filler's position.
        GridBagConstraints userListFillerConstraints = new GridBagConstraints();
        userListFillerConstraints.gridx = 0;
        userListFillerConstraints.gridy = childCount;
        userListFillerConstraints.weighty = 1.0;

        //Insert the updated filler.
        userListArea.add(filler, userListFillerConstraints);

        //Refresh the UI.
        userListArea.revalidate();
    }

    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE
        );
    }

    //All stream operations peformned here are responsible for incoming server events.
    private void run() throws IOException {
        Socket socket = null;
        try {
            socket = new Socket(serverAddress, 59001);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (line.startsWith("SUBMITNAME")) {
                	// Handle SUBMITNAME; Prompts the user to type username
                    out.println(getName());
                } else if (line.startsWith("NAMEACCEPTED")) {
                	// Handle NAMEACCEPTED; Set chatbox title to username and make chat editable
                    this.frame.setTitle("Chatter - " + line.substring(13));
                    textField.setEditable(true);
                } else if (line.startsWith("MESSAGE")) {
                	// Handle MESSAGE; Display message to chatbox
                    messageArea.append(line.substring(8) + "\n");
                } else if (line.startsWith("PRIVATE")) {
                	// Handle PRIVATE; Display message to chatbox to specific client (handled by server);
                    messageArea.append(line.substring(8) + "\n");
                }
                else if (line.startsWith("USERS"))
                {
                    users = new HashMap<Integer, User>();

                    String[] data = line.substring("USERS".length() + 1).split(","); //+1 to remove the space
                    for (int i = 0; i < data.length; i++)
                    {
                        String[] parts = data[i].split(":");
                        String ipAddress = parts[2];
                        if (ipAddress.startsWith("/"))
                            ipAddress = ipAddress.substring(1);
                        User user = new User(Integer.parseInt(parts[0]), parts[1], ipAddress);
                        users.put(user.getId(), user);
                        AddUserToPanel(user);
                    }
                }
                else if (line.startsWith("USER"))
                {
                    //Used for a status update about a connection.
                    String[] parts = line.substring("USER".length() + 1).split(":");
                    Integer id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    String ipAddress = parts[2];
                    if (ipAddress.startsWith("/"))
                        ipAddress = ipAddress.substring(1);
                    boolean status = Boolean.parseBoolean(parts[4]); //Skip 3 as port is sent along with the IP address.

                    boolean exists = users.containsKey(id);

                    if (status && !exists) //If they have connected add the user.
                    {
                        User user = new User(id, name, ipAddress);
                        users.put(user.getId(), user);

                        messageArea.append(user.getName() + " has connected.\n");

                        AddUserToPanel(user);
                    }
                    else if (!status && exists) //They have disconnected, remove them.
                    {
                        users.remove(id);

                        messageArea.append(name + " has disconnected.\n");

                        RemoveUserFromPanel(id);
                    }
                }
            }
        } finally {
            if (socket != null)
                socket.close();
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
    	// creates a new client with the ip address from the arguments 
    
        // if (args.length != 1) {
        //     System.err.println("Pass the server IP as the sole command line argument");
        //     return;
        // }
        //String host = "localhost";
        Client client = new Client("localhost");
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}
