package ws1415.SkatenightBackend.model;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ws1415.SkatenightBackend.transport.EventMetaData;

/**
 * Repräsentiert die vollständigen Daten eines Events inklusive der Metadaten.
 * @author Richard Schulze
 */
@Entity
public class Event {
    @Id
    private Long id;
    private BlobKey icon;
    private String title;
    private Date date;
    private int routeFieldFirst;
    private int routeFieldLast;
    private boolean notificationSend = false;
    private BlobKey headerImage;
    private Text description;
    private String meetingPlace;
    private int fee;

    @Load(unless = {EventMetaData.class})
    private ArrayList<String> memberList;
    @Load(unless = {EventMetaData.class})
    private List<BlobKey> images;
    @Load(unless = {EventMetaData.class})
    private Ref<Route> route;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BlobKey getIcon() {
        return icon;
    }

    public void setIcon(BlobKey icon) {
        this.icon = icon;
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

    public boolean isNotificationSend() {
        return notificationSend;
    }

    public void setNotificationSend(boolean notificationSend) {
        this.notificationSend = notificationSend;
    }

    public BlobKey getHeaderImage() {
        return headerImage;
    }

    public void setHeaderImage(BlobKey headerImage) {
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

    public ArrayList<String> getMemberList() {
        return memberList;
    }

    public void setMemberList(ArrayList<String> memberList) {
        this.memberList = memberList;
    }

    public List<BlobKey> getImages() {
        return images;
    }

    public void setImages(List<BlobKey> images) {
        this.images = images;
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
}
