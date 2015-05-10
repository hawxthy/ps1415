package ws1415.SkatenightBackend.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import javax.jdo.annotations.PersistenceCapable;

/**
 * Speichert eine Menge von Bildern zu einem Event bzw. in einer globalen Gallery.
 * @author Richard Schulze
 */
@Entity
public class Gallery {
    public static final long GLOBAL_GALLERY_ID = 1;

    @Id
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
