package ws1415.SkatenightBackend.model;

import com.google.appengine.api.blobstore.BlobKey;

import java.util.List;

/**
 * Repräsentiert eine Entity, die BlobKeys enthält. BlobKeyContainer können vom BlobstoreUploadHandler
 * verarbeitet werden.
 * @author Richard Schulze
 */
public interface BlobKeyContainer {

    /**
     * Setzt die BlobKeys der Entity. Die Implementierung der Methode ist dafür verantwortlich, dass
     * die BlobKeys von der Reihenfolge, wie der UploadHandler sie erhält, in die richtigen Properties
     * der Entity eingeordnet werden.
     * @param keys    Die zu speichernden BlobKeys.
     */
    void consumeBlobKeys(List<BlobKey> keys);
}
