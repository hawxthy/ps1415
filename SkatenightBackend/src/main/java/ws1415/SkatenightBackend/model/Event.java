package ws1415.SkatenightBackend.model;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ws1415.SkatenightBackend.RoleEndpoint;
import ws1415.SkatenightBackend.UserEndpoint;
import ws1415.SkatenightBackend.transport.EventMetaData;
import ws1415.SkatenightBackend.transport.EventParticipationData;

/**
 * Repräsentiert die vollständigen Daten eines Events inklusive der Metadaten.
 * @author Richard Schulze
 */
@Entity
public class Event implements BlobKeyContainer, GalleryContainer {
    @Id
    private Long id;
    private BlobKey icon;
    private String title;
    @Index
    private Date date;
    private int routeFieldFirst;
    private int routeFieldLast;
    private boolean notificationSend = false;
    private BlobKey headerImage;
    private Text description;
    private String meetingPlace;
    private int fee;

    @Load(unless = {EventMetaData.class})
    private Map<String, EventRole> memberList = new HashMap<>();
    @Load(unless = {EventMetaData.class})
    private Map<String, EventParticipationVisibility> memberVisibility = new HashMap<>();
    @Load(unless = {EventMetaData.class, EventParticipationData.class})
    private List<BlobKey> images = new LinkedList<>();
    @Load(unless = {EventMetaData.class, EventParticipationData.class})
    @Index
    private Ref<Route> route;
    @Load(unless = {EventMetaData.class, EventParticipationData.class})
    private List<Ref<Gallery>> galleries = new LinkedList<>();
    private List<DynamicField> dynamicFields = new LinkedList<>();

    /**
     * Speichert die Upload-URL für den Upload in den Blobstore. Dient nur der Übertragung an die App
     * und wird aus diesem Grund nicht persistiert.
     */
    @Ignore
    private String imagesUploadUrl;

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

    public Map<String, EventRole> getMemberList() {
        return memberList;
    }

    public void setMemberList(Map<String, EventRole> memberList) {
        this.memberList = memberList;
    }

    public Map<String, EventParticipationVisibility> getMemberVisibility() {
        return memberVisibility;
    }

    public void setMemberVisibility(Map<String, EventParticipationVisibility> memberVisibility) {
        this.memberVisibility = memberVisibility;
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

    public List<Gallery> getGalleries() {
        if (galleries == null) {
            return null;
        }
        List<Gallery> resolvedGalleries = new LinkedList<>();
        for (Ref<Gallery> g : galleries) {
            resolvedGalleries.add(g.get());
        }
        return resolvedGalleries;
    }

    public void setGalleries(List<Gallery> galleries) {
        if (galleries == null) {
            this.galleries = null;
        }
        this.galleries = new LinkedList<>();
        for (Gallery g : galleries) {
            this.galleries.add(Ref.create(g));
        }
    }

    public String getImagesUploadUrl() {
        return imagesUploadUrl;
    }

    public List<DynamicField> getDynamicFields() {
        return dynamicFields;
    }

    public void setDynamicFields(List<DynamicField> dynamicFields) {
        this.dynamicFields = dynamicFields;
    }

    public void setImagesUploadUrl(String imagesUploadUrl) {
        this.imagesUploadUrl = imagesUploadUrl;
    }

    @Override
    public void consumeBlobKeys(List<BlobKey> keys) {
        if (keys != null && keys.size() >= 2) {
            icon = keys.get(0);
            headerImage = keys.get(1);
            images = new LinkedList<>(keys.subList(2, keys.size()));
        }
    }

    @Override
    public void addGallery(User user, Gallery gallery) throws OAuthRequestException {
        if (gallery == null) {
            throw new IllegalArgumentException("null, as a gallery, can not be added");
        }

        // User prüfen
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (!canAddGallery(user)) {
            throw new OAuthRequestException("insufficient privileges");
        }
        galleries.add(Ref.create(gallery));
    }

    @Override
    public void removeGallery(User user, Gallery gallery) throws OAuthRequestException {
        if (galleries == null) {
            return;
        }
        if (gallery == null) {
            throw new IllegalArgumentException("null, as a gallery, can not be removed");
        }

        // User prüfen
        if (user == null) {
            throw new OAuthRequestException("no user submitted");
        }
        if (!canRemoveGallery(user)) {
            throw new OAuthRequestException("insufficient privileges");
        }

        galleries.remove(Ref.create(gallery));
    }

    @Override
    public boolean canAddGallery(User user) {
        return memberList.get(user.getEmail()) != null && memberList.get(user.getEmail()).hasPrivilege(Privilege.ADD_GALLERY);
    }

    @Override
    public boolean canEditGallery(User user) {
        return memberList.get(user.getEmail()) != null && memberList.get(user.getEmail()).hasPrivilege(Privilege.EDIT_GALLERY);
    }

    @Override
    public boolean canRemoveGallery(User user) {
        return memberList.get(user.getEmail()) != null && memberList.get(user.getEmail()).hasPrivilege(Privilege.REMOVE_GALLERY);
    }

    @Override
    public boolean canAddPictures(User user, Picture picture) {
        return memberList.get(user.getEmail()) != null && picture.getVisibility() == PictureVisibility.PUBLIC;
    }

    @Override
    public boolean canRemovePictures(User user, Picture picture) {
        return picture.getUploader().equals(user.getEmail()) || (memberList.get(user.getEmail()) != null && memberList.get(user.getEmail()).hasPrivilege(Privilege.REMOVE_PICTURES));
    }
}
