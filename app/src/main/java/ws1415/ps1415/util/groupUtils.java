package ws1415.ps1415.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.List;
import java.util.Map;


import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.R;
import ws1415.ps1415.activity.UsergroupActivity;
import ws1415.common.task.DeleteUserGroupTask;
import ws1415.common.task.JoinUserGroupTask;
import ws1415.common.task.LeaveUserGroupTask;
import ws1415.ps1415.fragment.AllUsergroupsFragment;
import ws1415.ps1415.fragment.MyUsergroupsFragment;

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
        for(Map.Entry<String, Object> e : group.getMemberRanks().entrySet()){
            String value = (String)e.getValue();
            if(value.equals(email)) return true;
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
                new JoinUserGroupTask(new ExtendedTaskDelegateAdapter<Void, Void>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Void aVoid) {
                        ((UsergroupActivity) activity).refresh();
                    }
                }).execute(userGroup.getName());
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
                new LeaveUserGroupTask(new ExtendedTaskDelegateAdapter<Void, Void>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Void aVoid) {
                        ((UsergroupActivity) activity).refresh();
                    }
                }).execute(userGroup.getName());
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
    public static AlertDialog createDialogDelete(final UsergroupActivity activity, final UserGroup userGroup){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(userGroup.getName());
        builder.setMessage(R.string.dialog_delete_group);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new DeleteUserGroupTask(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                        if (aBoolean != null && aBoolean == true) {
                            AllUsergroupsFragment allUsergroupsFragment =
                                    (AllUsergroupsFragment)activity.getAdapter().getItem(0);
                            allUsergroupsFragment.deleteUserGroupFromList(userGroup);
                            MyUsergroupsFragment myUsergroupsFragment =
                                    (MyUsergroupsFragment)activity.getAdapter().getItem(1);
                            myUsergroupsFragment.deleteUserGroupFromList(userGroup);
                        }
                    }
                }).execute(userGroup);
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
