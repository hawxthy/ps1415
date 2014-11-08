package ws1415.SkatenightBackend;

/**
 * Wrapper, der die Übertragung von boolean-Werten im Backend ermöglicht.
 * Notwendig, da die Google App Engine keine primitiven Datentypen in den Signaturen der
 * Api-Methoden erlaubt.
 * @author Richard, Daniel
 */
public class BooleanWrapper {
    public boolean value;

    /**
     * Erstellt einen Boolean-Wrapper mit dem Wert false.
     */
    public BooleanWrapper() {
        this(false);
    }

    /**
     * Erstellt einen Boolean-Wrapper für den angegebenen Wert.
     * @param initialValue Der Wert des Boolean-Wrapper.
     */
    public BooleanWrapper(boolean initialValue) {
        this.value = initialValue;
    }

}
