package ws1415.SkatenightBackend.transport;

import com.google.appengine.api.blobstore.BlobKey;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ws1415.SkatenightBackend.model.DynamicField;
import ws1415.SkatenightBackend.model.Event;
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
    private BlobKey headerImage;
    private String description;
    private String meetingPlace;
    private int fee;

    private Map<String, EventRole> memberList;
    private List<BlobKey> images;
    private Route route;
    private List<Gallery> galleries;
    private List<DynamicField> dynamicFields;

    public EventData(Event event) {
        id = event.getId();
        icon = event.getIcon();
        title = event.getTitle();
        date = event.getDate();
        headerImage = event.getHeaderImage();
        description = event.getDescription().getValue();
        meetingPlace = event.getMeetingPlace();
        fee = event.getFee();

        // TODO Nur sichtbare Teilnehmer kopieren
        memberList = event.getMemberList();
        images = event.getImages();
        route = event.getRoute();
        galleries = event.getGalleries();
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

    public List<Gallery> getGalleries() {
        return galleries;
    }

    public void setGalleries(List<Gallery> galleries) {
        this.galleries = galleries;
    }

    public List<DynamicField> getDynamicFields() {
        return dynamicFields;
    }

    public void setDynamicFields(List<DynamicField> dynamicFields) {
        this.dynamicFields = dynamicFields;
    }
}
