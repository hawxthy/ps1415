package ws1415.SkatenightBackend.model;

/**
 * Created by Bernd Eissing on 02.05.2015.
 */
public enum UserGroupType {
    NORMALGROUP(1),
    SECURITYGROUP(2);

    public int id;

    private UserGroupType(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }
}
