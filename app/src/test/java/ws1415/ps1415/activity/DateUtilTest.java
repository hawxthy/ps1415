package ws1415.ps1415.activity;

import org.junit.Test;

import ws1415.ps1415.util.DateUtil;

import static junit.framework.Assert.assertEquals;

/**
 * Beispiel JUnit Test für die Bachelorarbeit "Evaluation von Test Frameworks
 * für Android-Apps und ihre Integration zu einer Test-Infrastruktur".
 * Hier wird getestet, ob DateUtil das Datum richtig formatiert.
 * Dies ist ein Beispiel für einen lokalen Unit Test
 *
 * @author Tristan Rust
 */
public class DateUtilTest {

    /**
     * Verifiziert, ob das Datum richtig formatiert wird.
     */
    @Test
    public void testFormatMyDate() {
        // Instace der Singelton Klasse holen
        DateUtil dateUtil = DateUtil.getInstance();
        // Unixzeitstempel für den 1.1.1970 1:0 Uhr
        Long timestamp = new Long(1L);
        // Formatieren des Datums
        String sdate = dateUtil.formatMyDate(timestamp);
        // Verifikation, ob das Datum richtig formatiert wurde
        assertEquals("Am: 1.1.1970 um 1:0 Uhr", sdate);
    }

}




