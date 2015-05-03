package ws1415.SkatenightBackend.model;

/**
 * Aufzählungstyp für die Rechte, die in einer Gruppe vergeben werden können. Hier
 * sind Rechte definiert wie das Einladen von neuen Mitgliedern in die Gruppe usw.
 * Jedes Recht hat eine Id, welche mit der getId Methode abgerufen werden kann.
 *
 * Created by Bernd Eissing on 02.05.2015.
 */
public enum Right {
    DELETEGROUP(1),
    VISIBILITYMAP(2),
    VISIBILITYSELF(3),
    MESSAGE(4),
    POSTBLACKBOARD(5),
    COMMENTBOARDMESSAGE(6),
    CHANGEGROUPPICTURE(7),
    GLOBALMESSAGE(8),
    INVITEGROUP(9),
    CHANGERANK(10),
    DISTRIBUTERANK(11),
    DELETEMEMBER(12);

    private int id;

    private Right(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }
}
