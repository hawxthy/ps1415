package ws1415.SkatenightBackend.model;

/**
 * Created by Martin on 01.05.2015.
 */
public enum Gender {
    MALE(1),
    FEMALE(2),
    NA(3);

    private int id;

    private Gender(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public String getValue(int id){
        switch(id){
            case 1:
                return "Männlich";
            case 2:
                return "Weiblich";
            case 3:
                return "N.a";
            default:
                return null;
        }
    }
}
