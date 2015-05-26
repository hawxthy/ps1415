package ws1415.ps1415.model;

/**
 * Aufzählungstyp für die Rechte, die in einer Gruppe vergeben werden können. Hier
 * sind Rechte definiert wie das Einladen von neuen Mitgliedern in die Gruppe usw.
 * Jedes Recht hat eine Id, welche mit der getId Methode abgerufen werden kann.
 *
 * Created by Bernd Eissing on 02.05.2015.
 */
public enum Right {
    DELETEGROUP,
    POSTBLACKBOARD,
    COMMENTBOARDMESSAGE,
    CHANGEGROUPPICTURE,
    GLOBALMESSAGE,
    INVITEGROUP,
    DISTRIBUTERIGHTS,
    DELETEMEMBER,
    NEWMEMBERRIGHTS,
    FULLRIGHTS;
}
