package ws1415.ps1415.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;

import com.skatenight.skatenightAPI.model.UserGroup;

import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.UsergroupAdapter;
import ws1415.ps1415.fragment.UsergroupsInterface;
import ws1415.ps1415.task.DeleteUserGroupTask;
import ws1415.ps1415.task.JoinUserGroupTask;
import ws1415.ps1415.task.LeaveUserGroupTask;

/**
 * Created by Martin on 03.02.2015.
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
     * @param position
     */
    public static void createDialogJoin(final int position, final UsergroupAdapter mAdapter, final Fragment fragment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
        builder.setTitle(mAdapter.getItem(position).getName());
        builder.setMessage(R.string.dialog_join_group);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new JoinUserGroupTask((UsergroupsInterface) fragment).execute(mAdapter.getItem(position).getName());
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create();
        builder.show();
    }

    /**
     * Erstellt einen Dialog, welcher aufgerufen wird, wenn ein Item in der ListView lange
     * ausgewählt wird. In diesem Dialog kann man dann auswählen, ob man die ausgewählten
     * Gruppe verlassen möchte.
     *
     * @param position
     */
    public static void createDialogLeave(final int position, final UsergroupAdapter mAdapter, final Fragment fragment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
        builder.setTitle(mAdapter.getItem(position).getName());
        builder.setMessage(R.string.dialog_leave_group);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new LeaveUserGroupTask((UsergroupsInterface) fragment).execute(mAdapter.getItem(position).getName());
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create();
        builder.show();
    }


    /**
     * Erstellt einen Dialog, der den Benutzer darauf hinweist, dass er seiner selbst erstellten
     * Gruppen nicht verlassen kann.
     */
    public static void createDialogOwner(final int position, final UsergroupAdapter mAdapter, final Fragment fragment){
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
        builder.setTitle(mAdapter.getItem(position).getName());
        builder.setMessage(R.string.dialog_group_owner);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create();
        builder.show();
    }

    /**
     * Erstellt einen Dialog, der den Benutzer fragt, ob die ausgewählte Gruppe gelöscht werden
     * soll.
     */
    public static void createDialogDelete(final int position, final UsergroupAdapter mAdapter, final Fragment fragment){
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
        builder.setTitle(mAdapter.getItem(position).getName());
        builder.setMessage(R.string.dialog_delete_group);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new DeleteUserGroupTask((UsergroupsInterface) fragment).execute(mAdapter.getItem(position));
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create();
        builder.show();
    }

    /**
     * Erstellt einen Dialog, der den Benutzer darauf hinweist, dass er die Gruppe nicht löschen
     * kann, da er nicht der Ersteller ist.
     */
    public static void createDialogDeleteFailed(final int position, final UsergroupAdapter mAdapter, final Fragment fragment){
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
        builder.setTitle(mAdapter.getItem(position).getName());
        builder.setMessage(R.string.dialog_delete_failed_group);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create();
        builder.show();
    }

}
