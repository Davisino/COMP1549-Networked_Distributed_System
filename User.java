

public class User {
	private String id;
    private String name;
    private String address;
    private int port;
    private boolean isCoordinator;

    public User(String id, String name, String address, int port) {
    	this.id= id;
        this.name = name;
        this.address = address;
        this.port = port;
        this.isCoordinator = false;
    }
    public String getId() {
    	return id;
    }
    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public boolean isCoordinator() {
        return isCoordinator;
    }
    public void setCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }

    // Other methods such as equals(), hashCode(), toString() can be added as needed
}
