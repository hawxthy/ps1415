package ws1415.SkatenightBackend.transport;

/**
 * Dient zur Übertragung von String-Werten vom Server zum Clienten, da der Endpoint
 * keine String-Werte als Rückgabetyp akzeptiert.
 */
public class StringWrapper {
    public String string;

    public StringWrapper(String string){
        this.string = string;
    }
}
