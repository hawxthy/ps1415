package ws1415.SkatenightBackend.model;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;

import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert die vollständigen Daten eines Events inklusive der Metadaten.
 * @author Richard Schulze
 */
@Entity
public class Event {
    @Id
    private Long id;
    private Ref<Route> route;
    private int routeFieldFirst;
    private int routeFieldLast;
    private ArrayList<String> memberList;
    private boolean notificationSend = false;
    private String headerImage;
    private Text description;
    private String meetingPlace;
    private int fee;
    private List<String> images;
    @Parent
    @Load
    private Ref<EventMetaData> metaData;
    private Ref<Gallery> gallery;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoute(Route route){
        this.route = Ref.create(route);
    }

    public Route getRoute() {
        if (route != null) {
            return route.get();
        } else {
            return null;
        }
    }

    public int getRouteFieldFirst() {
        return routeFieldFirst;
    }

    public void setRouteFieldFirst(int routeFieldFirst) {
        this.routeFieldFirst = routeFieldFirst;
    }

    public int getRouteFieldLast() {
        return routeFieldLast;
    }

    public void setRouteFieldLast(int routeFieldLast) {
        this.routeFieldLast = routeFieldLast;
    }

    public void setMemberList(ArrayList<String> memberList) {
        this.memberList = memberList;
    }

    public ArrayList<String> getMemberList() {
        if (this.memberList == null) return new ArrayList<>();
        else return this.memberList;
    }

    public boolean isNotificationSend() {
        return notificationSend;
    }

    public void setNotificationSend(boolean notificationSend) {
        this.notificationSend = notificationSend;
    }

    public String getHeaderImage() {
        return headerImage;
    }

    public void setHeaderImage(String headerImage) {
        this.headerImage = headerImage;
    }

    public Text getDescription() {
        return description;
    }

    public void setDescription(Text description) {
        this.description = description;
    }

    public String getMeetingPlace() {
        return meetingPlace;
    }

    public void setMeetingPlace(String meetingPlace) {
        this.meetingPlace = meetingPlace;
    }

    public int getFee() {
        return fee;
    }

    public void setFee(int fee) {
        this.fee = fee;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public EventMetaData getMetaData() {
        if (metaData != null) {
            return metaData.get();
        } else {
            return null;
        }
    }

    public void setMetaData(EventMetaData metaData) {
        this.metaData = Ref.create(metaData);
    }

    public Gallery getGallery() {
        if (gallery != null) {
            return gallery.get();
        } else {
            return null;
        }
    }

    public void setGallery(Gallery gallery) {
        this.gallery = Ref.create(gallery);
    }
}
