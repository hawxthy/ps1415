package ws1415.SkatenightBackend.model;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.LinkedList;
import java.util.List;

/**
 * Repräsentiert eine Bilder-Gallery mit einem Titel und einer Menge von Bildern.
 * @author Richard Schulze
 */
@Entity
public class Gallery {
    @Id
    private Long id;
    private String title;
    private List<Ref<Picture>> pictures;

    // Referenz auf den Container, in dem die Gallery gespeichert ist
    private String containerClass;
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
}
