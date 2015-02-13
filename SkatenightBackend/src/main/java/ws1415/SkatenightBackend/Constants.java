package ws1415.SkatenightBackend;

/**
 * Created by Richard on 24.10.2014.
 */
public class Constants {
    /**
     * Die Basisadresse des Backends.
     */
    public static final String BASE_URL = "https://skatenight-ms-jenkins.appspot.com";
    /**
     * Client-ID für die normale App.
     */
    public static final String ANDROID_USER_CLIENT_ID = "1032268444653-kj1mpisvlrpl67e7db2fu2005aube7mu.apps.googleusercontent.com";
    /**
     * Client-ID für die Veranstalter-App.
     */
    public static final String ANDROID_HOST_CLIENT_ID = "1032268444653-83ih5mp5mguh66c012u0sobqe4oqoupr.apps.googleusercontent.com";
    /**
     * Client-ID für Webaufrufe.
     */
    public static final String WEB_CLIENT_ID = "1032268444653-7agre4q3eosqhlh92sq62hf5fan9jbv5.apps.googleusercontent.com";

    /**
     * Definiert die Webclient-ID als Android Audience, da aus den Apps der Zugriff auf das Back-
     * end über Webaufrufe realisiert ist.
     */
    public static final String ANDROID_AUDIENCE = WEB_CLIENT_ID;

    /**
     * Definiert die maximale Entfernung in m die ein Skater von einem nächsten Wegpunkt entfernt
     * sein darf um zu diesem zu gehören.
     */
    public static final float MAX_NEXT_WAYPOINT_DISTANCE = 10.0f;

    /**
     * Definiert die maximale Entfernung in m die ein Skater von einem beliebigen Wegpunkt entfernt
     * sein darf um zu diesem zu gehören.
     */
    public static final float MAX_ANY_WAYPOINT_DISTANCE = 50.0f;

    /**
     * Definiert die minimale Anzahl der Teilnehmer an einem beliebigen Wegpunkt um diesen als Teil
     * des Feldes zu werten.
     */
    public static final int MIN_WAYPOINT_MEMBER_COUNT = 1;

    /**
     * Der Public API-Key, der in der Developer Console für GCM angelegt wurde.
     */
    public static final String GCM_API_KEY = "AIzaSyBjeKzx_vzkUgQZdT83BB0BMD3gFebpet0";
}
