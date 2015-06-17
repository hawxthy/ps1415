package ws1415.SkatenightBackend.transport;

import java.util.List;

/**
 * Erweitert eine Liste von PictureMetaData-Objekten um eine String-Repräsentation des Cursors,
 * der durch die Abfrage aus dem Datastore entstanden ist. Dies ermöglicht die Abfrage weiterer
 * Objekte, indem bei dem Cursor fortgesetzt wird.
 * @author Richard Schulze
 */
public class PictureMetaDataList {
    private List<PictureMetaData> list;
    private String cursorString;

    public List<PictureMetaData> getList() {
        return list;
    }

    public void setList(List<PictureMetaData> list) {
        this.list = list;
    }

    public String getCursorString() {
        return cursorString;
    }

    public void setCursorString(String cursorString) {
        this.cursorString = cursorString;
    }
}
