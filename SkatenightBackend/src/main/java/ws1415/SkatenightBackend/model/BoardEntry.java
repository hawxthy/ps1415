package ws1415.SkatenightBackend.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by Bernd Eissing on 26.05.2015.
 */
@Entity
public class BoardEntry {
    @Id
    private Long id;
    private String writer;
    private String message;

    public BoardEntry() {
        // Konstruktor für GAE
    }

    public BoardEntry(String message, String writer) {
        this.writer = writer;
        this.message = message;
    }

    public Long getId(){
        return id;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}