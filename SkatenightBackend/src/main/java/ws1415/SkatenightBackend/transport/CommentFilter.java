package ws1415.SkatenightBackend.transport;

/**
 * Dient dem Filtern von Kommentaren, die über {@code CommentEndpoint.listComments()} abgerufen werden.
 * @author Richard Schulze
 */
public class CommentFilter {
    private long containerId;
    private String containerClass;
    private int limit;
    private String cursorString;

    public long getContainerId() {
        return containerId;
    }

    public void setContainerId(long containerId) {
        this.containerId = containerId;
    }

    public String getContainerClass() {
        return containerClass;
    }

    public void setContainerClass(String containerClass) {
        this.containerClass = containerClass;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getCursorString() {
        return cursorString;
    }

    public void setCursorString(String cursorString) {
        this.cursorString = cursorString;
    }
}
