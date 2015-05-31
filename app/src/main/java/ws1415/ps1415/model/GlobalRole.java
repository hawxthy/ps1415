package ws1415.ps1415.model;

import android.content.Context;

import ws1415.ps1415.R;

/**
 * Diese Enum-Klasse verwaltet die Rollen, die ein Benutzer innerhalb verschiedener
 * Umgebungen annehmen kann.
 *
 * @author Martin Wrodarczyk
 */
public enum GlobalRole {
    USER(1),
    ADMIN(2);

    private Integer id;

    private GlobalRole(Integer id){
        this.id = id;
    }

    public Integer getId(){
        return id;
    }

    public String getRepresentation(Context context){
        switch(id){
            case 1:
                return context.getString(R.string.user);
            case 2:
                return context.getString(R.string.admin);
            default:
                return null;
        }
    }

    public static GlobalRole getValue(Integer i)
    {
        for (GlobalRole r : GlobalRole.values()) {
            if (r.getId().equals(i))
                return r;
        }
        throw new IllegalArgumentException("Keine gültige Id für die Rolle");
    }
}
