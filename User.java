package cw;

// This class is the base class for the connection.
public class User {
    private int id = -1;
    private String address;
    protected String name;
    protected boolean isCoordinator;
    
    public User(int ID, String name, String address) {
        this.id = ID;
        this.name = name;
        this.address = address;
        this.isCoordinator = false;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public boolean isCoordinator() {
        return isCoordinator;
    }
    public void setCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }


}
