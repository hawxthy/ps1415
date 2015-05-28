package ws1415.SkatenightBackend.transport;

/**
 * Repräsentiert die Daten eines Filters, anhand dessen Picture-Objekte aus dem Datastore geladen werden.
 * @author Richard Schulze
 */
public class PictureFilter {
    /**
     * Speichert den Cursor, der beim vorherigen Abruf von PictureMetaData-Objekten entstanden ist,
     * damit an dieser Stelle weiter gemacht werden kann.
     */
    private String cursorString;
    /**
     * Anzahl der Objekte, die maximal abgerufen werden sollen.
     */
    private int limit;
    /**
     * Die ID der Gallery, für die Bilder abgerufen werden sollen.
     */
    private Long galleryId;
    /**
     * Die ID des Users (E-Mail), für den die Bilder abgerufen werden sollen.
     */
    private String userId;

    public String getCursorString() {
        return cursorString;
    }

    public void setCursorString(String cursorString) {
        this.cursorString = cursorString;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Long getGalleryId() {
        return galleryId;
    }

    public void setGalleryId(Long galleryId) {
        this.galleryId = galleryId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
