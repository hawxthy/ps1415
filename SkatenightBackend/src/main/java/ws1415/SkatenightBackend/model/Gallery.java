package ws1415.SkatenightBackend.model;

import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonIgnore;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import java.util.LinkedList;
import java.util.List;

import ws1415.SkatenightBackend.transport.GalleryMetaData;

/**
 * Repräsentiert eine Bilder-Gallery mit einem Titel und einer Menge von Bildern.
 * @author Richard Schulze
 */
@Entity
public class Gallery {
    @Id
    private Long id;
    private String title;
    @Load(unless = {GalleryMetaData.class})
    @JsonIgnore
    private List<Ref<Picture>> pictures = new LinkedList<>();

    // Referenz auf den Container, in dem die Gallery gespeichert ist
    @Index
    private String containerClass;
    @Index
    private long containerId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Picture> getPictures() {
        if (pictures == null) {
            return null;
        }
        List<Picture> resolvedPictures = new LinkedList<>();
        for (Ref<Picture> p : pictures) {
            resolvedPictures.add(p.get());
        }
        return resolvedPictures;
    }

    public void setPictures(List<Picture> pictures) {
        if (pictures == null) {
            this.pictures = null;
        }
        this.pictures = new LinkedList<>();
        for (Picture p : pictures) {
            this.pictures.add(Ref.create(p));
        }
    }

    public String getContainerClass() {
        return containerClass;
    }

    public void setContainerClass(String containerClass) {
        this.containerClass = containerClass;
    }

    public long getContainerId() {
        return containerId;
    }

    public void setContainerId(long containerId) {
        this.containerId = containerId;
    }

    public void addPicture(Picture picture) {
        if (picture == null) {
            throw new NullPointerException("null, as a picture, can not be added");
        }
        pictures.add(Ref.create(picture));
    }

    public void removePicture(Picture picture) {
        if (picture == null) {
            throw new NullPointerException("null, as a picture, can not be removed");
        }
        pictures.remove(Ref.create(picture));
    }
}
