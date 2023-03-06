

public class User {
    private String name;
    private String address;
    private int port;
    private boolean isCoordinator;

    public User(String name, String address, int port) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.isCoordinator = false;
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
