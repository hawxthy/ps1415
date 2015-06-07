package ws1415.SkatenightBackend.transport;

import java.util.List;

import ws1415.SkatenightBackend.model.UserGroup;

/**
 * @author  Bernd Eissing on 07.06.2015.
 */
public class UserGroupMetaDataList {
    List<UserGroup> metaDatas;
    String webCursorString;

    public List<UserGroup> getMetaDatas() {
        return metaDatas;
    }

    public void setMetaDatas(List<UserGroup> metaDatas) {
        this.metaDatas = metaDatas;
    }

    public String getWebCursorString() {
        return webCursorString;
    }

    public void setWebCursorString(String webCursorString) {
        this.webCursorString = webCursorString;
    }
}
