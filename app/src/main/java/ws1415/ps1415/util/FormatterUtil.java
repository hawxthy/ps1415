package ws1415.ps1415.util;

import java.util.regex.Pattern;

/**
 * Stellt Service-Methoden zur Formatierung und zum Parsen von Daten bereit.
 * @author Richard Schulze
 */
public abstract class FormatterUtil {
    private static final Pattern currencyPattern = Pattern.compile("^(0|[1-9]\\d*)(,\\d{2})? ?€?$");

    /**
     * Prüft, ob der übergebene String null, leer oder im Datumsformat ist.
     * @param s    Der zu prüfende String.
     * @return true, wenn der String null, leer
     */
    public static boolean isCurrencyString(String s) {
        return s == null || s.isEmpty() || currencyPattern.matcher(s).matches();
    }

    /**
     * Formatiert den angegebenen Cent-Betrag als Währungsstring.
     * @param cent    Der zu formatierende Betrag.
     * @return Der Betrag als Währungsstring formatiert.
     */
    public static String formatCents(int cent) {
        int euro = cent / 100;
        cent = cent - euro * 100;
        if (cent < 10) {
            return euro + ",0" + cent + " €";
        } else {
            return euro + "," + cent + " €";
        }
    }

    /**
     * Gibt den als Währungsstring angegebenen Betrag in Cent zurück.
     * @param s    Der zu parsende String.
     * @return Der geparste Betrag in Cent. -1, falls der angegebene String nicht das richtige Format besitzt.
     */
    public static int getCentsFromCurrencyString(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        try {
            String euro = currencyPattern.matcher(s).group(1);
            String cent = currencyPattern.matcher(s).group(2);
            if (cent != null && !cent.isEmpty()) {
                // Komma abschneiden
                cent = cent.substring(0, cent.length() - 1);
            }
            return Integer.parseInt(euro) * 100 + Integer.parseInt(cent);
        } catch(IllegalStateException ex) {
            throw new IllegalArgumentException("string does not match currency format");
        }
    }

}
