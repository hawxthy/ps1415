package ws1415.SkatenightBackend.model;

import com.google.appengine.api.blobstore.BlobKey;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Bernd Eissing on 26.05.2015.
 */
@Entity
public class BoardEntry {
    @Id
    private Long id;
    private String writer;
    private String message;
    private Date date;
    @Load
    private ArrayList<Comment> comments;
    private BlobKey blobKey;
    @Ignore
    private String uploadUrl;

    public BoardEntry() {
        // Konstruktor für GAE
    }

    public BoardEntry(String message, String writer) {
        date = new Date();
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public BlobKey getBlobKey() {
        return blobKey;
    }

    public void setBlobKey(BlobKey blobKey) {
        this.blobKey = blobKey;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public BoardEntry addComment(String comment, String writer){
        if(comments == null){
            comments = new ArrayList<>();
        }
        comments.add(new Comment(comment, writer));
        return this;
    }

    @Index
    public static class Comment{
        private Date date;
        private String writer;
        private String message;

        public Comment(){
            // Konstruktor für GAE
        }

        public Comment(String message, String writer){
            date = new Date();
            this.message = message;
            this.writer = writer;
        }

        public Date getDate(){
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
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
    }
}