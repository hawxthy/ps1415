package ws1415.ps1415.util;

import junit.framework.TestCase;

import java.text.ParseException;

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
            fail("Trying to decode null should throw a NullPointerException.");
        }
        catch (ParseException e) {
            fail("Trying to decode null should throw a NullPointerException, not a ParseException.");
        }
        catch (NullPointerException e) {
            assertEquals(null, e.getMessage());
        }
    }
}
