package ws1415.SkatenightBackend;

/**
 * Created by Richard on 24.10.2014.
 */
public class Constants {
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
}
