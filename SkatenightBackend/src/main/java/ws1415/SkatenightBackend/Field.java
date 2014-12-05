package ws1415.SkatenightBackend;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonCreator;

import java.io.Serializable;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * Klasse für die Informationsfelder einer Veranstaltung
 * <p/>
 * Created by Bernd Eissing on 02.12.2014.
 */
@PersistenceCapable
public class Field {

    /**
     * Enum um die Felder zu spezifizieren.
     */
    public enum TYPE {
        SIMPLETEXT, FEE, DATE, TIME, ROUTE, PICTURE, LINK, TITLE, LOCATION, DESCRIPTION
    }

    @Persistent
    private String title;
    @Persistent
    private String value;
    @Persistent
    private TYPE type;

    public Field(String title, TYPE type) {
        this.title = title;
        this.type = type;
    }

    @JsonCreator
    public Field() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }
}
