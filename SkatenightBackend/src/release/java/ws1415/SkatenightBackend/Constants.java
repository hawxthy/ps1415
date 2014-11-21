package ws1415.SkatenightBackend;

/**
 * Created by Richard on 24.10.2014.
 */
public class Constants {
    /**
     * Client-ID für die normale App.
     */
    public static final String ANDROID_USER_CLIENT_ID = "37947570052-0730rlarj87fm7pvl930qma6gmhbciuu.apps.googleusercontent.com";
    /**
     * Client-ID für die Veranstalter-App.
     */
    public static final String ANDROID_HOST_CLIENT_ID = "37947570052-1frhohnd9vbt3130433qabd1pjgv9jvq.apps.googleusercontent.com";
    /**
     * Client-ID für Webaufrufe.
     */
    public static final String WEB_CLIENT_ID = "37947570052-dk3rjhgran1s38gscv6va2rmmv2bei8r.apps.googleusercontent.com";

    /**
     * Definiert die Webclient-ID als Android Audience, da aus den Apps der Zugriff auf das Back-
     * end über Webaufrufe realisiert ist.
     */
    public static final String ANDROID_AUDIENCE = WEB_CLIENT_ID;
}
