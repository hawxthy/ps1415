package ws1415.SkatenightBackend;

import com.google.appengine.api.datastore.Text;

/**
 * Created by Bernd Eissing, Martin Wrodarczyk on 21.10.2014.
 */
public class Event {
    private String title;
    private String date;
    private String fee;
    private String location;
    private Text description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Text getDescription() {
        return description;
    }

    public void setDescription(Text description) {
        this.description = description;
    }
}
