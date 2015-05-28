package ws1415.SkatenightBackend.transport;

import java.util.List;

/**
 * Erweitert eine Liste von EventMetaData-Objekten um eine String-Repräsentation des Cursors,
 * der durch die Abfrage aus dem Datastore entstanden ist. Dies ermöglicht die Abfrage weiterer
 * Objekte, indem bei dem Cursor fortgesetzt wird.
 * @author Richard Schulze
 */
public class EventMetaDataList {
    private List<EventMetaData> list;
    private String cursorString;

    public List<EventMetaData> getList() {
        return list;
    }

    public void setList(List<EventMetaData> list) {
        this.list = list;
    }

    public String getCursorString() {
        return cursorString;
    }

    public void setCursorString(String cursorString) {
        this.cursorString = cursorString;
    }
}
