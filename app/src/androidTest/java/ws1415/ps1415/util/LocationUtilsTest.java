package ws1415.ps1415.util;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pascalotto on 28.10.14.
 */
public class LocationUtilsTest extends TestCase {

    public void testDoubleEncoder() {
        // 0.0 -> "?"
        assertEquals("?", LocationUtils.encodeDouble(0.0));
        assertEquals("?", LocationUtils.encodeDouble(-0.0));

        // Es werden maximal 5 Nachkommastellen gespeichert.
        assertEquals("?", LocationUtils.encodeDouble(0.000002));

        // 51.96592 -> "_rd|H"
        assertEquals("_rd|H", LocationUtils.encodeDouble(51.96592));

        // -7.60353 -> "`alm@"
        assertEquals("`alm@", LocationUtils.encodeDouble(-7.60353));

        // Es muss möglich sein den Wert als int darzustellen nachdem dieser mit 1e5 multipliziert wurde.
        try {
            LocationUtils.encodeDouble(3e6);
            fail("Trying to encode 3e6 should throw an IllegalArgumentException.");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Value out of bounds. Must be between 1e5 * 2³¹-1 and 1e5 * -2³¹.", e.getMessage());
        }
        try {
            LocationUtils.encodeDouble(-3e6);
            fail("Trying to encode -3e6 should throw an IllegalArgumentException.");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Value out of bounds. Must be between 1e5 * 2³¹-1 and 1e5 * -2³¹.", e.getMessage());
        }
    }

    public void testDoubleDecoder() {
        // "?" -> 0.0
        try {
            assertEquals(0.0, LocationUtils.decodeDouble("?"));
        }
        catch (Throwable t) {
            fail("Decoding should not throw an exception.");
        }

        // "_rd|H" -> 51.96592
        try {
            assertEquals(51.96592, LocationUtils.decodeDouble("_rd|H"));
        }
        catch (Throwable t) {
            fail("Decoding should not throw an exception.");
        }

        // "`alm@" -> -7.60353
        try {
            assertEquals(-7.60353, LocationUtils.decodeDouble("`alm@"));
        }
        catch (Throwable t) {
            fail("Decoding should not throw an exception.");
        }

        // "`alm" hat kein End-Bit.
        try {
            LocationUtils.decodeDouble("`alm");
            fail("Trying to decode '`alm' should throw a ParseException.");
        }
        catch (ParseException e) {
            assertEquals("End block not found. (at offset 4)", e.getMessage());
        }

        // Leerer String kann nicht decodiert werden.
        try {
            LocationUtils.decodeDouble("");
            fail("Trying to decode empty string should throw a ParseException.");
        }
        catch (ParseException e) {
            assertEquals("End block not found. (at offset 0)", e.getMessage());
        }

        // null kann nicht decodiert werden.
        try {
            LocationUtils.decodeDouble(null);
            fail("Trying to decode null should throw an IllegalArgumentException.");
        }
        catch (ParseException e) {
            fail("Trying to decode null should throw an IllegalArgumentException, not a ParseException.");
        }
        catch (IllegalArgumentException e) {
            assertEquals("String may not be null.", e.getMessage());
        }
    }

    public void testLocationEncoder() {
        // (0.0, 0.0) -> "??"
        Location locationA = new Location(this.getClass().getSimpleName());
        locationA.setLatitude(0.0);
        locationA.setLongitude(0.0);
        assertEquals("??", LocationUtils.encodeLocation(locationA));

        // (51.96592, -7.60353) -> "_rd|H`alm@"
        Location locationB = new Location(this.getClass().getSimpleName());
        locationB.setLatitude(51.96592);
        locationB.setLongitude(-7.60353);
        assertEquals("_rd|H`alm@", LocationUtils.encodeLocation(locationB));

        // Ungültige Location
        Location locationC = new Location(this.getClass().getSimpleName());
        locationC.setLatitude(4e6);
        locationC.setLongitude(-4e6);
        try {
            LocationUtils.encodeLocation(locationC);
            fail("Trying to encode location (4e6, -4e6) should throw an IllegalArgumentException.");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Value out of bounds. Must be between 1e5 * 2³¹-1 and 1e5 * -2³¹.", e.getMessage());
        }

        try {
            LocationUtils.encodeLocation(null);
            fail("Trying to encode location null should throw an IllegalArgumentException.");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Location may not be null.", e.getMessage());
        }
    }

    public void testLocationDecoder() {
        // "??" -> (0.0, 0.0)
        try {
            Location l = LocationUtils.decodeLocation("??");
            assertEquals(0.0, l.getLatitude());
            assertEquals(0.0, l.getLongitude());
        }
        catch (Throwable t) {
            fail("Decoding should not throw an exception.");
        }

        // "_rd|H`alm@" -> (51.96592, -7.60353)
        try {
            Location l = LocationUtils.decodeLocation("_rd|H`alm@");
            assertEquals(51.96592, l.getLatitude());
            assertEquals(-7.60353, l.getLongitude());
        }
        catch (Throwable t) {
            fail("Decoding should not throw an exception.");
        }

        // "_rd|h`alm@" hat kein End-Bit für den Längengrad.
        try {
            LocationUtils.decodeLocation("_rd|h`alm@");
            fail("Trying to decode '_rd|h`alm@' should throw a ParseException.");
        }
        catch (ParseException e) {
            assertEquals("End block not found. (at offset 7)", e.getMessage());
        }

        // Leerer String kann nicht decodiert werden.
        try {
            LocationUtils.decodeLocation("");
            fail("Trying to decode empty string should throw a ParseException.");
        }
        catch (ParseException e) {
            assertEquals("End block not found. (at offset 0)", e.getMessage());
        }

        // null kann nicht decodiert werden.
        try {
            LocationUtils.decodeLocation(null);
            fail("Trying to decode null should throw an IllegalArgumentException.");
        }
        catch (ParseException e) {
            fail("Trying to decode null should throw an IllegalArgumentException, not a ParseException.");
        }
        catch (IllegalArgumentException e) {
            assertEquals("String may not be null.", e.getMessage());
        }
    }

    public void testPolylineEncoder() {
        List<LatLng> line = new ArrayList<LatLng>(5);
        line.add(new LatLng(51.96592, -7.60353));
        line.add(new LatLng(51.96593, -7.60352));
        line.add(new LatLng(51.0, -7.0));

        String encoded = LocationUtils.encodePolyline(line);

        // Es kann beim Codieren zu Rundungsfehlern kommen. Es sollte aber ungefähr das Gleiche rauskommen.
        try {
            List<LatLng> decoded = LocationUtils.decodePolyline(encoded);
            assertEquals(line.size(), decoded.size());
            for (int i = 0; i < line.size(); i++) {
                assertEquals(line.get(i).latitude, decoded.get(i).latitude, 0.00002);
                assertEquals(line.get(i).longitude, decoded.get(i).longitude, 0.00002);
            }
        }
        catch (Throwable t) {
            fail("Decoding should not throw an exception.");
        }

        // Grenzwerte müssen bei LatLng nicht getestet werden, da sie im Konstruktor ersetzt werden.

        // Leere Liste kann nicht codiert werden.
        try {
            LocationUtils.encodePolyline(new ArrayList<LatLng>());
            fail("Trying to encode empty list should throw an IllegalArgumentException.");
        }
        catch (IllegalArgumentException e) {
            assertEquals("List must contain at least one coordinate.", e.getMessage());
        }

        // null kann nicht codiert werden.
        try {
            LocationUtils.encodePolyline(null);
            fail("Trying to encode null should throw an IllegalArgumentException.");
        }
        catch (IllegalArgumentException e) {
            assertEquals("List must contain at least one coordinate.", e.getMessage());
        }

        // Das 1. 'h' müsste groß geschrieben sein.
        try {
            LocationUtils.decodePolyline("_rd|h`alm@AA~qd|Haalm@");
            fail("Trying to decode '_rd|h`alm@AA~qd|Haalm@' should throw a ParseException.");
        }
        catch (ParseException e) {
            assertEquals("End block not found. (at offset 7)", e.getMessage());
        }

        // Leerer String kann nicht decodiert werden.
        try {
            LocationUtils.decodePolyline("");
            fail("Trying to decode empty string should throw a ParseException.");
        }
        catch (ParseException e) {
            assertEquals("End block not found. (at offset 0)", e.getMessage());
        }

        // null kann nicht decodiert werden.
        try {
            LocationUtils.decodePolyline(null);
            fail("Trying to decode null should throw an IllegalArgumentException.");
        }
        catch (ParseException e) {
            fail("Trying to decode null should throw an IllegalArgumentException, not a ParseException.");
        }
        catch (IllegalArgumentException e) {
            assertEquals("String may not be null.", e.getMessage());
        }
    }
}
