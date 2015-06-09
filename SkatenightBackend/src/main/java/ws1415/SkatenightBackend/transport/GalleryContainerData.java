package ws1415.SkatenightBackend.transport;

import com.google.appengine.api.users.User;

import java.util.LinkedList;
import java.util.List;

import ws1415.SkatenightBackend.model.GalleryContainer;
import ws1415.SkatenightBackend.model.PictureVisibility;
import ws1415.SkatenightBackend.model.Privilege;

/**
 * Transportiert die Metadaten einer Galerie inkl. der Rechte, die der aufrufende Benutzer in der Galerie hat.
 * @author Richard Schulze
 */
public class GalleryContainerData {
    private Long containerId;
    private String containerClass;
    private List<Privilege> privileges = new LinkedList<>();
    private PictureVisibility minPictureVisbility;

    public GalleryContainerData(User user, Long containerId, String containerClass, GalleryContainer container) {
        this.containerId = containerId;
        this.containerClass = containerClass;
        if (container.canAddGallery(user)) {
            privileges.add(Privilege.ADD_GALLERY);
        }
        if (container.canEditGallery(user)) {
            privileges.add(Privilege.EDIT_GALLERY);
        }
        if (container.canRemoveGallery(user)) {
            privileges.add(Privilege.REMOVE_GALLERY);
        }
        if (container.canAddPictures(user)) {
            privileges.add(Privilege.ADD_PICTURE);
        }
        if (container.canRemovePictures(user)) {
            privileges.add(Privilege.REMOVE_PICTURES);
        }
        minPictureVisbility = container.getMinPictureVisibility();
    }

    public Long getContainerId() {
        return containerId;
    }

    public void setContainerId(Long containerId) {
        this.containerId = containerId;
    }

    public String getContainerClass() {
        return containerClass;
    }

    public void setContainerClass(String containerClass) {
        this.containerClass = containerClass;
    }

    public List<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Privilege> privileges) {
        this.privileges = privileges;
    }

    public PictureVisibility getMinPictureVisbility() {
        return minPictureVisbility;
    }

    public void setMinPictureVisbility(PictureVisibility minPictureVisbility) {
        this.minPictureVisbility = minPictureVisbility;
    }
}
