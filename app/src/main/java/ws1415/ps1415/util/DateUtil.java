package ws1415.ps1415.util;

import java.util.Calendar;
import java.util.Date;


/**
 * @author Bernd Eissing on 08.06.2015.
 */
public class DateUtil {
    private static DateUtil instance;

    private DateUtil(){}

    public static DateUtil getInstance(){
        if(instance == null){
            instance = new DateUtil();
        }
        return instance;
    }

    /**
     * Erstellt aus einem übergebenen Date im Long Foramt einen String, der einem
     * Datum entspricht. Das Datum wird in Tag.Monat.Jahr um Stunden:Minuten angegeben.
     *
     * @param dateValue Der wert eines Date Objektes im Long Format
     * @return Einen String, der einem Datum entspricht
     */
    public String formatMyDate(Long dateValue){
        Date date = new Date(dateValue);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);

        return "Am: "+day+"."+month+"."+year+" um "+hours+":"+minutes+" Uhr";
    }
}
