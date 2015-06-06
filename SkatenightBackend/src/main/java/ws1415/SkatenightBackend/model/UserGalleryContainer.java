package ws1415.SkatenightBackend.model;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.LinkedList;
import java.util.List;

import ws1415.SkatenightBackend.RoleEndpoint;

/**
 * Ein GalleryContainer für benutzer. Dies ist aus folgenden Gründen als eigene Klasse implementiert:
 * <ul>
 *     <li>Schnittstelle zwischen DataNucleus und Objectify: Da der User mit dataNucleus persistiert
 *     wird, ist eine gesonderte Verarbeitung notwendig. Das Hinzufügen und Entfernen von Gallerien
 *     kann nicht über Objectify enthaltende Endpoint-Methoden geschehen, wenn der Nutzer die
 *     Gallerien direkt enthält.</li>
 *     <li>Implementieren des Interface GalleryContainer: Zur Unterstützung der Standardfunktionen
 *     von Gallerien müsste die Klasse EndUser das Interface GalleryContainer implementieren. Dies
 *     ist nicht vorgesehen, um keine Änderungen an der EndUser-Klasse vornehmen zu müssen, die zurzeit
 *     unter Bearbeitung von Martin Wrodarczyk im Rahmen seiner Bachelorarbeit ist.</li>
 *     <li>Referenz einer Gallery auf den Container: Gallerien enthalten eine Referenz auf ihren
 *     Container. Diese Referenz ist durch den Datastore-Kind und die ID des Containers in Form eines
 *     longs realisiert. Da EndUser einen String als Key haben, ist keine direkte Referenz möglich.</li>
 * </ul>
 * @author Richard Schulze
 */
@Entity
public class UserGalleryContainer implements GalleryContainer {
    @Id
    private Long id;
    private List<Ref<Gallery>> galleries = new LinkedList<>();
    @Index
    private String user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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
        return user.getEmail().equals(this.user);
    }

    @Override
    public boolean canEditGallery(User user) {
        return user.getEmail().equals(this.user);
    }

    @Override
    public boolean canRemoveGallery(User user) {
        return user.getEmail().equals(this.user);
    }

    @Override
    public boolean canAddPictures(User user, Picture picture) {
        return user.getEmail().equals(this.user);
    }

    @Override
    public boolean canRemovePictures(User user, Picture picture) {
        return user.getEmail().equals(this.user) || new RoleEndpoint().isAdmin(user.getEmail()).value;
    }
}
