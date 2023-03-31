package cw;
//Necessary Imports
import java.awt.*; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


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

    public Client(String serverAddress) {
        this.serverAddress = serverAddress;

        textField.setEditable(false);
        messageArea.setEditable(false);
//        setting the x, y , width and height of the textfield, message area and user list area.
        textField.setBounds(150, 410, 330, 30);
        textField.setColumns(10);
        messageArea.setBounds(70, 120, 530, 250);
        userListArea.setBounds(620, 160, 80, 350);
    

        frame.setLayout(null);
        
        // User Details Button
        JButton button = new JButton("Get User Details");
        button.setBounds(620, 90, 200,40);
        userListArea.add(button);
        button.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		for (User user: users.values()) {
                   out.println("/details " + " > ID: " + user.getId() + " , Name: " + user.getName() + " , IP Address: " + user.getAddress());
            	}
            }
        });
        
        JLabel title = new JLabel("Welcome to ChatBox");
        title.setFont(new Font("Arial", Font.PLAIN, 30));
        title.setBounds(250, 30, 400,50);
		frame.getContentPane().add(title);
        
        JLabel lblNewLabel = new JLabel("Send Message:");
		lblNewLabel.setBounds(620, 130, 200,50);
		lblNewLabel.setFont(new Font("Arial", Font.PLAIN, 15));
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Message:");
		lblNewLabel_1.setBounds(70, 100, 200, 16);
		frame.getContentPane().add(lblNewLabel_1);
		
		JLabel lblNewLabel_3 = new JLabel("Press enter to send message !..");
		lblNewLabel_3.setBounds(150, 390, 200, 16);
		frame.getContentPane().add(lblNewLabel_3);
		
		JLabel lblNewLabel_2 = new JLabel("Type Message");
		lblNewLabel_2.setBounds(50, 410, 200, 16);
		frame.getContentPane().add(lblNewLabel_2);
       
        frame.setLayout(null);
        frame.getContentPane().add(textField);
        frame.getContentPane().add(messageArea);
        frame.getContentPane().add(userListArea);
        frame.getContentPane().add(button);
        AddUserToPanel(new User(0, "All", "0.0.0.0"));
        userListButtons.getElements().nextElement().setSelected(true);


        frame.setSize(850, 550);

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

    // This function adds a user button to the UI witht the name of the user.
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

    // This function takes an id as an input and removes the button associated with
    // that id.
    private void RemoveUserFromPanel(Integer id)
    {
        for (Component child : userListArea.getComponents())
        {
            if (child instanceof JRadioButton)
            {
            	
                JRadioButton button = (JRadioButton)child;
             
                if (button.getClientProperty("user_id").equals(id))
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
    // This function counts how many current button we have on the client.
    private int GetUsersUICount()
    {
        int count = 0;
        for (Component child : userListArea.getComponents())
            if (child instanceof JRadioButton)
                count++;
        return count;
    }
    // this function updates the user panel from the UI
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

    // this function is called every time a new user joins.
    // it is the function that prompts the user to enter their name
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
                	// Here we are updating the list of users for each client
                	// to contain all the users that are already connected to the server.
                	
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
        	// executed after the socket connection ends,
        	// we dispose this client.
            if (socket != null)
                socket.close();
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
 
        Client client = new Client("localhost");
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}
