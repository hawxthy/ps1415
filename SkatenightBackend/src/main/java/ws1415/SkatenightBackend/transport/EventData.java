package ws1415.SkatenightBackend.transport;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.users.User;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ws1415.SkatenightBackend.model.DynamicField;
import ws1415.SkatenightBackend.model.Event;
import ws1415.SkatenightBackend.model.EventParticipationVisibility;
import ws1415.SkatenightBackend.model.EventRole;
import ws1415.SkatenightBackend.model.Gallery;
import ws1415.SkatenightBackend.model.Route;

/**
 * Transportklasse, die alle Daten des Events überträgt, die der aufrufende Benutzer einsehen darf.
 * @author Richard Schulze
 */
public class EventData {
    private Long id;
    private BlobKey icon;
    private String title;
    private Date date;
    private int routeFieldFirst;
    private int routeFieldLast;
    private BlobKey headerImage;
    private String description;
    private String meetingPlace;
    private int fee;

    private Map<String, EventRole> memberList;
    private EventParticipationVisibility participationVisibility;
    private List<BlobKey> images;
    private Route route;
    private List<DynamicField> dynamicFields;

    public EventData(User user, Event event) {
        id = event.getId();
        icon = event.getIcon();
        title = event.getTitle();
        date = event.getDate();
        routeFieldFirst = event.getRouteFieldFirst();
        routeFieldLast = event.getRouteFieldLast();
        headerImage = event.getHeaderImage();
        description = event.getDescription().getValue();
        meetingPlace = event.getMeetingPlace();
        fee = event.getFee();

        memberList = event.getMemberList();
        participationVisibility = event.getMemberVisibility().get(user.getEmail());
        images = event.getImages();
        route = event.getRoute();
        dynamicFields = event.getDynamicFields();
    }

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

    public int getRouteFieldLast() {
        return routeFieldLast;
    }

    public void setRouteFieldLast(int routeFieldLast) {
        this.routeFieldLast = routeFieldLast;
    }

    public BlobKey getHeaderImage() {
        return headerImage;
    }

    public void setHeaderImage(BlobKey headerImage) {
        this.headerImage = headerImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
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

    public Map<String, EventRole> getMemberList() {
        return memberList;
    }

    public void setMemberList(Map<String, EventRole> memberList) {
        this.memberList = memberList;
    }

    public EventParticipationVisibility getParticipationVisibility() {
        return participationVisibility;
    }

    public void setParticipationVisibility(EventParticipationVisibility participationVisibility) {
        this.participationVisibility = participationVisibility;
    }

    public List<BlobKey> getImages() {
        return images;
    }

    public void setImages(List<BlobKey> images) {
        this.images = images;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public List<DynamicField> getDynamicFields() {
        return dynamicFields;
    }

    public void setDynamicFields(List<DynamicField> dynamicFields) {
        this.dynamicFields = dynamicFields;
    }

    public int getRouteFieldFirst() {
        return routeFieldFirst;
    }

    public void setRouteFieldFirst(int routeFieldFirst) {
        this.routeFieldFirst = routeFieldFirst;
    }
}
