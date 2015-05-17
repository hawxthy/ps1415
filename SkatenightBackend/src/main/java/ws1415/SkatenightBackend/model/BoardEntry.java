package ws1415.SkatenightBackend.model;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.datanucleus.annotations.Unowned;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by Bernd Eissing on 02.05.2015.
 */
@Entity
public class BoardEntry {
    @Id
    private Long id;
    private Picture picture;
    private String message;
    private String writer;
    private ArrayList<String> comments;

    public BoardEntry(){
        // Konstruktor für GAE
        comments = new ArrayList<>();
    }

    public BoardEntry(Picture picture, String message, String writer){
        this.picture = picture;
        this.message = message;
        this.writer = writer;
        comments = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Picture getPicture() {
        return picture;
    }

    public void setPicture(Picture picture) {
        this.picture = picture;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    public void setComments(ArrayList<String> comments) {
        this.comments = comments;
    }

    public void addComment(String comment){
        // TODO auf Reihenfolge achten, wenn das nicht schon gemacht wird
        if(comment != null && !comment.isEmpty()){
            getComments().add(comment);
        }
    }

    public void removeComment(String comment){
        if(comment != null && !comment.isEmpty()){
            getComments().remove(comment);
        }
    }
}
