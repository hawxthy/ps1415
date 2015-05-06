package ws1415.common.model;

import java.util.Date;

/**
 * Die Message-Klasse repräsentiert eine Nachricht.
 *
 * @author Martin Wrodarczyk
 */
public class Message {
    private long _id;
    private Date sendDate;
    private String content;
    private LocalMessageType type;

    public Message(long _id, Date sendDate, String content, LocalMessageType type) {
        this._id = _id;
        this.sendDate = sendDate;
        this.content = content;
        this.type = type;
    }

    public Message(Date sendDate, String content, LocalMessageType type) {
        this.sendDate = sendDate;
        this.content = content;
        this.type = type;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
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

    public LocalMessageType getType() {
        return type;
    }

    public void setType(LocalMessageType type) {
        this.type = type;
    }
}
