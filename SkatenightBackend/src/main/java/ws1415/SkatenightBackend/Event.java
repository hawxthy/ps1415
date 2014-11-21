package ws1415.SkatenightBackend;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.datanucleus.annotations.Unowned;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Datenmodell für eine Veranstaltung.
 * Created by Richard Schulze, Bernd Eissing, Martin Wrodarczyk on 21.10.2014.
 */
@PersistenceCapable
public class Event{
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    @Persistent
    private String title;
    @Persistent
    private Date date;
    @Persistent
    private String fee;
    @Persistent
    private String location;
    @Persistent
    private Text description;
    @Persistent(defaultFetchGroup = "true")
    @Unowned
    private Route route;
    @Persistent
    private int routeFieldFirst;
    @Persistent
    private int routeFieldLast;

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

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
