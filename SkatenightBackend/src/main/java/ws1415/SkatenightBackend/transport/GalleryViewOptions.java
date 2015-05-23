package ws1415.SkatenightBackend.transport;

/**
 * Repräsentiert die Daten eines Filters, anhand dessen Picture-Objekte aus dem Datastore geladen werden.
 * @author Richard Schulze
 */
public class GalleryViewOptions {
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
     * Die ID der Gallery, für die Bilder abgerufen werden.
     */
    private long galleryId;

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

    public long getGalleryId() {
        return galleryId;
    }

    public void setGalleryId(long galleryId) {
        this.galleryId = galleryId;
    }
}
