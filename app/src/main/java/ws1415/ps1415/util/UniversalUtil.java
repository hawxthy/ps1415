package ws1415.ps1415.util;

import java.util.Calendar;

/**
 * Created by Martin on 23.05.2015.
 */
public abstract class UniversalUtil {

    public static int calculateAge(Calendar dob){
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR))
            age--;
        return age;
    }

}
