package ws1415.SkatenightBackend.transport;

/**
 * @author Bernd Eissing on 07.06.2015.
 */
public class UserGroupFilter {
    private String cursorString;
    private int limit;

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
}
