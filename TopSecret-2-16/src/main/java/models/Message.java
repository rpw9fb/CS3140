package models;

public class Message {

    private final int id;
    private final int recipientAgentId;
    private final String body;
    private final String senderUsername;
    private final boolean isRead;

    public Message(int id, int recipientAgentId, String body, String senderUsername, boolean isRead) {
        this.id = id;
        this.recipientAgentId = recipientAgentId;
        this.body = body;
        this.senderUsername = senderUsername;
        this.isRead = isRead;
    }

    public Message(int id, int recipientAgentId, String body, String senderUsername) {
        this(id, recipientAgentId, body, senderUsername, false);
    }

    public int getId() {
        return id;
    }

    /** Agent the message is addressed to. */
    public int getRecipientAgentId() {
        return recipientAgentId;
    }

    public String getBody() {
        return body;
    }

    /** Username of the user who composed the message (terminal session). */
    public String getSenderUsername() {
        return senderUsername;
    }

    public boolean isRead()
    {
        return isRead;
    }
}
