package ws1415.SkatenightBackend;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import ws1415.SkatenightBackend.model.UserGroup;
import ws1415.SkatenightBackend.model.GroupMetaData;

/**
 * Objectify service wrapper zum registrieren der persistenten
 * Entitäten Klassen
 *
 * Created by Bernd on 14.05.2015.
 */
public class OfyService {
    // registriert die Entitäten Klassen statisch
    static {
        ObjectifyService.register(UserGroup.class);
        ObjectifyService.register(GroupMetaData.class);
    }

    public static Objectify ofy(){
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory(){
        return ObjectifyService.factory();
    }
}
