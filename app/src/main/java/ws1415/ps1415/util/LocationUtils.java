package ws1415.ps1415.util;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Beinhaltet einige Hilfsfunktionen zum codieren und decodieren von Positionsdaten.
 */
public final class LocationUtils {
    private static final String LOG_TAG = LocationUtils.class.getSimpleName();

    private LocationUtils() {
    }

    /**
     * Codiert den gegebenen double Wert als String. Der Wert wird mit 1e5 multipliziert und
     * als int gespeichert. Somit werden maximal 5 Nachkommastellen codiert. Außerdem muss
     * der Wert multipliziert mit 1e5 zwischen 2³¹-1 und -2³¹ sein.
     *
     * @param value Zu codierender Wert.
     * @return String des codierten Wertes.
     */
    public static String encodeDouble(double value) {
        value = Math.round(value * 1e5);
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE)
            throw new IllegalArgumentException("Value out of bounds. Must be between 1e5 * 2³¹-1 and 1e5 * -2³¹.");

        return encodeValue((int) value);
    }

    /**
     * Decodiert den gegebenen String als double.
     *
     * @param encoded Zu decodierender Wert.
     * @return Decodierter Wert.
     * @throws ParseException Falls kein gültiger String übergeben wurde.
     */
    public static double decodeDouble(String encoded) throws ParseException {
        return ((double) decodeValue(new StringBuffer(encoded))) / 1e5;
    }

    /**
     * Codiert die gegebene Liste von Koordinaten als String.
     *
     * @param line Zu codierende Werte.
     * @return String der codierten Werte.
     */
    public static String encodePolyline(List<LatLng> line) {
        if (line == null || line.size() <= 0)
            throw new IllegalArgumentException("Line must contain at least 1 coordinate.");

        StringBuffer buffer = new StringBuffer();
        LatLng previous = line.get(0);

        buffer.append(encodeDouble(previous.latitude));
        buffer.append(encodeDouble(previous.longitude));

        for (int i = 1; i < line.size(); i++) {
            LatLng current = line.get(i);
            buffer.append(encodeDouble(current.latitude - previous.latitude));
            buffer.append(encodeDouble(current.longitude - previous.longitude));
        }
        return buffer.toString();
    }

    /**
     * Decodiert den gegebenen String als Liste von Koordinaten.
     *
     * @param encoded Zu decodierende Werte.
     * @return Liste der decodierten Koordinaten.
     * @throws ParseException Falls kein gültiger String übergeben wurde.
     */
    public static List<LatLng> decodePolyline(String encoded) throws ParseException {
        List<LatLng> line = new ArrayList<LatLng>();

        StringBuffer buffer = new StringBuffer(encoded);

        while (buffer.length() > 0) {
            double lat;
            double lon;
            lat = ((double) decodeValue(buffer)) / 1e5;
            lon = ((double) decodeValue(buffer)) / 1e5;

            if (line.size() > 0) {
                lat += line.get(line.size() - 1).latitude;
                lon += line.get(line.size() - 1).longitude;
            }
            line.add(new LatLng(lat, lon));
        }
        return line;
    }

    /**
     * Codiert die gegebene Location als String.
     *
     * @param location Zu codierende Location.
     * @return String der codierten Location.
     */
    public static String encodeLocation(Location location) {
        if (location == null)
            throw new NullPointerException();

        StringBuffer buffer = new StringBuffer();
        buffer.append(encodeDouble(location.getLatitude()));
        buffer.append(encodeDouble(location.getLongitude()));
        return buffer.toString();
    }

    /**
     * Decodiet den gegebenen String als Location.
     *
     * @param encoded Zu decodierende Location als String.
     * @return Decodierte Location.
     * @throws ParseException Falls kein gültiger String übergeben wurde.
     */
    public static Location decodeLocation(String encoded) throws ParseException {
        StringBuffer buffer = new StringBuffer(encoded);
        Location location = new Location(LocationUtils.class.getSimpleName());
        location.setLatitude(((double)decodeValue(buffer))/1e5);
        location.setLongitude(((double)decodeValue(buffer))/1e5);
        return location;
    }

    /**
     * Codiert den gegebenen Wert und gibt ihn als String zurück.
     *
     * @param value Zu codierender Wert als int.
     * @return Codierter Wert.
     */
    private static String encodeValue(int value) {
        int number = value << 1;

        // Falls value negativ ist wird number invertiert
        // (das Bit 0 welches durch den shift 0 ist wird
        // somit zur 1 und zeigt das Vorzeichen an).
        if (value < 0) number = ~number;

        StringBuffer buffer = new StringBuffer();

        // number wird in 5 Bit Zahlen unterteilt. Falls es noch
        // mindestens eine weitere 5 Bit Zahl gibt, wird diese
        // als char zum Buffer hinzugefügt.
        while (number >= 0x20 || number < 0) {
            // Durch das AND mit 0x1f werden außer den Bits 0-4
            // alle auf 0 gesetzt. Anschließend wird durch das OR
            // Bit 5 auf 1 gesetzt um zu zeigen, dass noch weitere
            // Zahlen folgen. Zum Schluss wird noch 63 addiert um
            // ein sichtbare ASCII Zeichen zu erhalten.
            buffer.append((char) (((number & 0x1f) | 0x20) + 63));

            // 5 Bit wurden verarbeitet und number wird für die nächsten
            // 5 vorbereitet.
            number >>>= 5;
        }

        // Nun muss noch die letzte Zahl codiert werden:
        // Die Methode ist fast die gleiche wie zuvor. Es
        // wird lediglich das 5. Bit nicht gesetzt.
        buffer.append((char) ((number & 0x1f) + 63));

        return buffer.toString();
    }

    /**
     * Decodiert den gegebenen String im StringBuffer und gibt den Wert als int zurück.
     * Die gelesenen Zeichen werden aus dem StringBuffer entfernt.
     *
     * @param encoded Codierter String als StringBuffer.
     * @return Decodierter Wert als int.
     * @throws ParseException Falls nicht genügend Zeichen zum parsen vorhanden sind.
     */
    private static int decodeValue(StringBuffer encoded) throws ParseException {
        int length = encoded.length();

        int currentBlock; // Aktuelles Zeichen als Zahl
        int number = 0; // Vollständige codierte Zahl
        int offset = 0;

        // Es wird ein Zeichen gelesen und decodiert. Das Ergebnis wird in
        // number gespeichert. Wenn Bit 6 des decodierten Zeichen gesetzt ist
        // folgt noch ein weiteres Zeichen, sonst war das soeben gelesene das
        // Letzte.
        do {
            if (encoded.length() <= 0) {
                // Es gibt keine weiteren Zeichen mehr und es wurde noch kein Block
                // mit End-Bit gefunden.
                throw new ParseException("End block not found.", length);
            }

            // Dem codierten Zeichen werden die zuvor addierten 63 abgezogen
            currentBlock = encoded.charAt(0) - 63;
            encoded.deleteCharAt(0);

            // Die ersten 5 Bit des aktuellen Block werden um den offset geshiftet
            // und in number eingefügt.
            number |= (currentBlock & 0x1f) << offset;

            // Da 5 Bit hinzugefügt wurden, wird offset um 5 erhöht.
            offset += 5;
        }
        while (currentBlock >= 0x20);

        if ((number & 1) != 0) {
            // Bit 1 gesetzt -> negative Zahl
            // Das Vorzeichenbit fällt durch shift nach rechts weg und das
            // Ergebnis wird invertiert. Bit 31 wird 1, da Zahl negativ.
            number = ~(number >>> 1);
        }
        else {
            // Bit 1 nicht gesetzt -> positive Zahl
            // Es muss lediglich das Vorzeichenbit wegfallen
            number >>>= 1;
        }
        return number;
    }
}