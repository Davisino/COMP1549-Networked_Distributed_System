

public class Message {
	private String id;
	private String senderName;
	private String receiverName;
	private String content;
	private String timestampt;

    public Message(String id, String senderName, String receiverName, String content, String timestampt) {
    	this.id= id;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.content = content;
        this.timestampt = timestampt;
    }
    public String getContent() {
    	return "<" + timestampt + ">" + senderName + " to " + receiverName + ": " + content;
    }

    // Other methods such as equals(), hashCode(), toString() can be added as needed
}
