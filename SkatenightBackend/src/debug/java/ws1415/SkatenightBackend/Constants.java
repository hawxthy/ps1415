package ws1415.SkatenightBackend;

/**
 * Created by Richard on 24.10.2014.
 */
public class Constants {
    /**
     * Die Basisadresse des Backends.
     */
    public static final String BASE_URL = "https://skatenight-ms-testing.appspot.com";
    /**
     * Client-ID für die normale App.
     */
    public static final String ANDROID_USER_CLIENT_ID = "644721617929-pre5qlom1cvr0e071tlsg6kvvcps8ngt.apps.googleusercontent.com";
    /**
     * Client-ID für Webaufrufe.
     */
    public static final String WEB_CLIENT_ID = "644721617929-asqioscun7ejlghnfrj7rpuashjb0tb4.apps.googleusercontent.com";

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
    public static final String GCM_API_KEY = "AIzaSyCpwhxda1Lb5E61_fybZq2iSJgViZu3QNM";

    /**
     * Definiert die Benutzer, der als Administrator beim Start der Servers erstellt werden sollen,
     * falls diese nicht bereits existiert.
     */
    public static final String[] INITIAL_ADMINS = {"skatenight.host@gmail.com", "martin.wrod@googlemail.com",
            "Bernd.Eissing@gmail.com", "richard-schulze@online.de"};
}
