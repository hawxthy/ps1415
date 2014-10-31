package ws1415.SkatenightBackend;

import com.google.appengine.api.datastore.Text;
import java.util.Date;

/**
 * Created by Bernd Eissing, Martin Wrodarczyk on 21.10.2014.
 */
public class Event {
    private String title;
    private Date date;
    private String fee;
    private String location;
    private Text description;
    private Route route;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }
}
