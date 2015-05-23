package ws1415.ps1415.model;

/**
 * Die Conversation-Klasse repräsentiert ein Gespräch
 *
 * @author Martin Wrodarczyk
 */
public class Conversation {
    private String email;
    private byte[] picture;
    private String firstName;
    private String lastName;
    private int countNewMessages;
    private Message lastMessage;

    public Conversation(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        countNewMessages = 0;
        lastMessage = null;
    }

    public Conversation(String email, byte[] picture, String firstName, String lastName) {
        this.email = email;
        this.picture = picture;
        this.firstName = firstName;
        this.lastName = lastName;
        countNewMessages = 0;
        lastMessage = null;
    }

    public Conversation(String email, byte[] picture, String firstName, String lastName, int countNewMessages, Message lastMessage) {
        this.email = email;
        this.picture = picture;
        this.firstName = firstName;
        this.lastName = lastName;
        this.countNewMessages = countNewMessages;
        this.lastMessage = lastMessage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getCountNewMessages() {
        return countNewMessages;
    }

    public void setCountNewMessages(int countNewMessages) {
        this.countNewMessages = countNewMessages;
    }
}
