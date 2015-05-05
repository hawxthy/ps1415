package ws1415.ps1415.model;

import java.util.Date;

/**
 * Die Message-Klasse repräsentiert eine Nachricht.
 *
 * @author Martin Wrodarczyk
 */
public class Message {
    private int _id;
    private Date sendDate;
    private String content;
    private MessageType type;

    public Message(int _id, Date sendDate, String content, MessageType type) {
        this._id = _id;
        this.sendDate = sendDate;
        this.content = content;
        this.type = type;
    }

    public Message(Date sendDate, String content, MessageType type) {
        this.sendDate = sendDate;
        this.content = content;
        this.type = type;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
