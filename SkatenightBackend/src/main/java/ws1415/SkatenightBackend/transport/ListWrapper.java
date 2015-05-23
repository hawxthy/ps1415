package ws1415.SkatenightBackend.transport;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper, der die Übertragung von String Listen im Backend ermöglicht.
 * Notwendig, da die Google App Engine keine primitiven Datentypen als Rückgabewert erlaubt.
 *
 * @author Martin Wrodarczyk
 */
public class ListWrapper {
    public List<String> stringList;

    public ListWrapper(){
        stringList = new ArrayList<>();
    }

    public ListWrapper(List<String> stringList){
        this.stringList = stringList;
    }
}
