package ws1415.SkatenightBackend;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Text;

import java.io.Serializable;

/**
 * Klasse für die Informationsfelder einer Veranstaltung
 * <p/>
 * Created by Bernd Eissing on 02.12.2014.
 */
public class Field implements Serializable {
    private String title;
    private String value;
    private Text data;
    private int type;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Text getData() {
        return data;
    }

    public void setData(Text data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
