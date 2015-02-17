package ws1415.ps1415.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.activity.UsergroupActivity;
import ws1415.ps1415.task.DeleteUserGroupTask;
import ws1415.ps1415.task.JoinUserGroupTask;
import ws1415.ps1415.task.LeaveUserGroupTask;

/**
 * Utility Klasse für die Nutzergruppen. Unterstützende Klasse um Nutzergruppen beizutreten, zu
 * verlassen und zu löschen.
 *
 * @author Martin Wrodarczyk
 */
public class groupUtils {
    /**
     * Prüft ob der Benutzer mit der angegebenen Email-Adresse Mitglied in der Gruppe ist.
     *
     * @param email Email-Adresse des Benutzers
     * @param group Gruppe
     * @return
     */
    public static boolean isUserInGroup(String email, UserGroup group){
        List<String> members = group.getMembers();
        for(int i=0; i<members.size(); i++){
            if(members.get(i).equals(email)) return true;
        }
        return false;
    }

    /**
     * Prüft ob der Benutzer mit der angegebenen Email-Adresse der Ersteller der Gruppe ist.
     *
     * @param email Email-Adresse des Benutzers
     * @param group Gruppe
     * @return
     */
    public static boolean isCreator(String email, UserGroup group) {
        String creator = group.getCreator().getEmail();
        if (creator.equals(email)) return true;
        return false;
    }

    /**
     * Erstellt einen Dialog, welcher aufgerufen wird, wenn ein Item in der ListView lange
     * ausgewählt wird. In diesem Dialog kann man dann auswählen, ob man der ausgewählten
     * Gruppe beitreten möchte.
     *
     * @param activity Activity, der den Dialog anzeigt
     * @param userGroup Nutzergruppe, die ausgewählt wurde
     */
    public static AlertDialog createDialogJoin(final FragmentActivity activity, final UserGroup userGroup) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(userGroup.getName());
        builder.setMessage(R.string.dialog_join_group);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new JoinUserGroupTask((UsergroupActivity) activity).execute(userGroup.getName());
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return builder.create();
    }

    /**
     * Erstellt einen Dialog, welcher aufgerufen wird, wenn ein Item in der ListView lange
     * ausgewählt wird. In diesem Dialog kann man dann auswählen, ob man die ausgewählten
     * Gruppe verlassen möchte.
     *
     * @param activity Activity, der den Dialog anzeigt
     * @param userGroup Nutzergruppe, die ausgewählt wurde
     */
    public static AlertDialog createDialogLeave(final FragmentActivity activity, final UserGroup userGroup) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(userGroup.getName());
        builder.setMessage(R.string.dialog_leave_group);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new LeaveUserGroupTask((UsergroupActivity) activity).execute(userGroup.getName());
                PrefManager.setGroupVisibility(activity, userGroup.getName(), false);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return builder.create();
    }


    /**
     * Erstellt einen Dialog, der den Benutzer darauf hinweist, dass er seiner selbst erstellten
     * Gruppen nicht verlassen kann.
     *
     * @param activity Activity, der den Dialog anzeigt
     * @param userGroup Nutzergruppe, die ausgewählt wurde
     */
    public static AlertDialog createDialogOwner(final FragmentActivity activity, final UserGroup userGroup){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(userGroup.getName());
        builder.setMessage(R.string.dialog_group_owner);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return builder.create();
    }

    /**
     * Erstellt einen Dialog, der den Benutzer fragt, ob die ausgewählte Gruppe gelöscht werden
     * soll.
     *
     * @param activity Activity, der den Dialog anzeigt
     * @param userGroup Nutzergruppe, die ausgewählt wurde
     */
    public static AlertDialog createDialogDelete(final FragmentActivity activity, final UserGroup userGroup){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(userGroup.getName());
        builder.setMessage(R.string.dialog_delete_group);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new DeleteUserGroupTask((UsergroupActivity)activity).execute(userGroup);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return builder.create();
    }

    /**
     * Erstellt einen Dialog, der den Benutzer darauf hinweist, dass er die Gruppe nicht löschen
     * kann, da er nicht der Ersteller ist.
     *
     * @param activity Activity, der den Dialog anzeigt
     * @param userGroup Nutzergruppe, die ausgewählt wurde
     */
    public static AlertDialog createDialogDeleteFailed(final FragmentActivity activity, final UserGroup userGroup){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(userGroup.getName());
        builder.setMessage(R.string.dialog_delete_failed_group);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return builder.create();
    }

}
