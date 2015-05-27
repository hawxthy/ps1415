package ws1415.common.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Created by Bernd Eissing on 27.05.2015.
 */
public class Password {
    // Definieren der BCrypt worload, welche beim generieren von Passwörtern  benutzt wird.
    // Man kann hier 10-31 angeben
    private static int workload =12;

    /**
     * Diese Methode generiert einen String, der 60 Zeichen lang ist der
     * ein Passwort repräsentiert. Hier wird auch automatisch ein gewisser
     * Salt Wert generiert, der gleichzeitig im String gespeichert ist.
     *
     * @param passwordPlain Das Password als Plaintext
     * @return Einen String mit 60 Zeichen der dem Passwort entspricht
     */
    public static String hashPassword(String passwordPlain){
        String salt = BCrypt.gensalt(workload);
        String hashed_password = BCrypt.hashpw(passwordPlain, salt);

        return hashed_password;
    }

    public static boolean checkPassword(String passwordPlain, String passwordHash){
        boolean passwordVerified = false;

        if(null == passwordHash || !passwordHash.startsWith("$2a$"))
            throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison");

        passwordVerified = BCrypt.checkpw(passwordPlain, passwordHash);

        return passwordVerified;
    }
}
