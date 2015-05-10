package ws1415.common.util;

/**
 * Aufzählungstyp für die Rechte, die in einer Gruppe vergeben werden können. Hier
 * sind Rechte definiert wie das Einladen von neuen Mitgliedern in die Gruppe usw.
 * Jedes Recht hat eine Id, welche mit der getId Methode abgerufen werden kann.
 *
 * Created by Bernd Eissing on 02.05.2015.
 */
public enum Right {
    DELETEGROUP,
    VISIBILITYMAP,
    VISIBILITYSELF,
    MESSAGE,
    POSTBLACKBOARD,
    COMMENTBOARDMESSAGE,
    CHANGEGROUPPICTURE,
    GLOBALMESSAGE,
    INVITEGROUP,
    CHANGERANK,
    DISTRIBUTERANK,
    DELETEMEMBER;
}
