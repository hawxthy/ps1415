package ws1415.ps1415.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.EventMetaData;
import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupFilter;
import com.skatenight.skatenightAPI.model.UserGroupMetaData;
import com.skatenight.skatenightAPI.model.UserGroupMetaDataList;

import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.activity.InviteUsersToGroupActivity;
import ws1415.ps1415.activity.ListUserGroupsActivity;
import ws1415.ps1415.activity.MyUserGroupsActivity;
import ws1415.ps1415.controller.EventController;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.GroupImageLoader;
import ws1415.ps1415.util.PrefManager;


/**
 * Dieser Adapter wird für das Anzeigen der Usergruppen in der Liste genutzt.
 *
 * @author Bernd Eissing
 */
public class UsergroupAdapter extends BaseAdapter {
    private List<UserGroup> groupList;
    private Context context;
    private LayoutInflater inflater;
    private int maximum;
    // Attribut zum speichern, ob gerade Gruppen geladen werden
    private boolean fetching = false;
    private boolean fetchingDone = false;
    private UserGroupFilter filter;
    private int count = 0;

    /**
     * Konstruktor, der den Inhalt der Liste festlegt;
     *
     * @param context Context, von dem aus der Adapter aufgerufen wird.
     * @param maximum Maximale Anzahl der Einträge, oder -1 für unbegrenzt.
     */
    public UsergroupAdapter(Context context, UserGroupFilter filter, int maximum) {
        if (maximum > -1 && maximum < groupList.size()) {
            throw new IllegalArgumentException("Liste zu groß");
        }
        if (filter == null) {
            throw new IllegalArgumentException("no filter submitted");
        }
        this.maximum = maximum;
        this.context = context;
        this.filter = filter;
        this.groupList = new ArrayList<>();
        if (context != null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        fetchNewGroups();
    }

    /**
     * Gibt die Anzahl der Nutzergruppen zurück.
     *
     * @return Anzahl der Nutzergruppen
     */
    @Override
    public int getCount() {
        if (groupList == null) return 0;
        else return groupList.size();
    }

    /**
     * Gibt die Nutzergruppe an der Stelle i in der Liste zurück.
     *
     * @param i Stelle
     * @return Nutzergruppe an Stelle i
     */
    @Override
    public UserGroup getItem(int i) {
        if (fetching && i == groupList.size()) {
            return null;
        }
        return groupList.get(i);
    }

    /**
     * Gibt die ID der Nutzergruppe in der Liste zurück.
     *
     * @param i Stelle der Nutzergruppe
     * @return ID der Nutzergruppe
     */
    public long getItemId(int i) {
        // nicht benutzen
        return i;
    }

    /**
     * Klasse zum Halten der GUI Elemente, damit convertView die alten Objekte übernehmen kann.
     */
    private class Holder {
        private ImageView groupImage;
        private TextView groupName;
        private TextView groupCount;
        private ImageView hiddenSecurityButton;
        private ImageView hiddenCancelButton;
        private EditText hiddenGroupName;
    }

    /**
     * Methode zum Füllen der ListView mit Items.
     *
     * @param position
     * @param convertView
     * @param viewGroup
     * @return
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        final Holder holder;
        if (position + 1 == groupList.size() && !fetchingDone) fetchNewGroups();

        if (convertView == null) {
            holder = new Holder();
            convertView = inflater.inflate(R.layout.list_view_item_usergroup, viewGroup, false);
            holder.groupImage = (ImageView) convertView.findViewById(R.id.user_group_list_view_item_image);
            holder.groupName = (TextView) convertView.findViewById(R.id.user_group_list_view_item_title);
            holder.groupCount = (TextView) convertView.findViewById(R.id.user_group_list_view_item_count);
            holder.hiddenSecurityButton = (ImageView) convertView.findViewById(R.id.hidden_security_group_button);
            holder.hiddenCancelButton = (ImageView) convertView.findViewById(R.id.hidden_cancel_button);
            holder.hiddenGroupName = (EditText) convertView.findViewById(R.id.hidden_group_name);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        // Image Laden
        if (getItem(position).getBlobKey() != null) {
            GroupImageLoader.getInstance().setGroupImageToImageView(context, getItem(position).getBlobKey().getKeyString(), holder.groupImage);
        } else {
            GroupImageLoader.getInstance().setGroupImageToImageView(context, null, holder.groupImage);
        }
        //Attribute Setzen
        holder.groupName.setText(getItem(position).getName());
        holder.groupCount.setText(context.getString(R.string.usergroup_member_count) + Integer.toString(getItem(position).getMemberCount()));
        holder.hiddenGroupName.setText(getItem(position).getName());

        if (context instanceof ListUserGroupsActivity) {
            holder.hiddenSecurityButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_security_black_24dp));
            holder.hiddenSecurityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertadd = new AlertDialog.Builder(context);
                    alertadd.setMessage(R.string.securityGroupInfo);
                    alertadd.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertadd.show();
                }
            });
            if (getItem(position).getPrivat()) {
                holder.hiddenSecurityButton.setVisibility(View.VISIBLE);
            }
        } else {
            if (count < 6) {
                final MyUserGroupsActivity activity = (MyUserGroupsActivity) context;
                holder.hiddenSecurityButton.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_add_black_24dp));
                if (PrefManager.getGroupVisibility(context, getItem(position).getName())) {
                    holder.hiddenSecurityButton.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_visibility_black_24dp));
                    holder.hiddenCancelButton.setVisibility(View.VISIBLE);
                    count++;
                }
                holder.hiddenSecurityButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!PrefManager.getGroupVisibility(context, holder.hiddenGroupName.getText().toString()) && count < 5) {
                            holder.hiddenSecurityButton.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_visibility_black_24dp));
                            holder.hiddenCancelButton.setVisibility(View.VISIBLE);
                            PrefManager.setGroupVisibility(context, holder.hiddenGroupName.getText().toString(), true);
                            count++;
                        }
                    }
                });
                holder.hiddenCancelButton.setImageDrawable(activity.getResources().getDrawable(R.drawable.remove_icon_in_red));
                if (!PrefManager.getGroupVisibility(context, getItem(position).getName())) {
                    holder.hiddenSecurityButton.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_add_black_24dp));
                    holder.hiddenCancelButton.setVisibility(View.GONE);
                }
                holder.hiddenCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (PrefManager.getGroupVisibility(context, holder.hiddenGroupName.getText().toString())) {
                            holder.hiddenSecurityButton.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_add_black_24dp));
                            holder.hiddenCancelButton.setVisibility(View.GONE);
                            PrefManager.setGroupVisibility(context, holder.hiddenGroupName.getText().toString(), false);
                            count--;
                        }
                    }
                });
            }else{
                Toast.makeText(context, R.string.tooManyVisibleGroups, Toast.LENGTH_LONG).show();
            }
        }


        return convertView;
    }


    /**
     * Ruft neue Nutzergruppen ab, wenn aufgerufen und im moment noch keine
     * abgerufen werden.
     */
    protected void fetchNewGroups() {
        if (!fetching) {
            if (context instanceof Activity) {
                final Activity activity = (Activity) context;
                if (activity != null) activity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
                fetching = true;
                GroupController.getInstance().listUserGroupMetaDatas(new ExtendedTaskDelegateAdapter<Void, UserGroupMetaDataList>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, UserGroupMetaDataList userGroupMetaDataList) {
                        if (userGroupMetaDataList.getMetaDatas() == null) {
                            Toast.makeText(context, R.string.noMore, Toast.LENGTH_LONG).show();
                            activity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
                            fetchingDone = true;
                        } else {
                            groupList.addAll(userGroupMetaDataList.getMetaDatas());
                            deliverCursorString(userGroupMetaDataList.getWebCursorString());
                            notifyDataSetChanged();
                            if (userGroupMetaDataList.getMetaDatas().size() < filter.getLimit()) {
                                fetchingDone = true;
                            }
                            if (activity != null)
                                activity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        }
                        fetching = false;
                    }

                    @Override
                    public void taskFailed(ExtendedTask task, String message) {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        if (activity != null)
                            activity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                    }
                }, filter);
            }

        }
    }

    /**
     * Übergibt dem Filter den cursorstring, damit beim nächsten Aufruf von
     * listUserGroupMetaDatas der alte Cursor verwendet wird.
     *
     * @param cursorString
     */
    protected void deliverCursorString(String cursorString) {
        this.filter.setCursorString(cursorString);
    }

    /**
     * Entfernt die UserGroup.
     *
     * @param userGroup UserGroup
     */
    public void removeListItem(UserGroup userGroup) {
        groupList.remove(userGroup);
        notifyDataSetChanged();
    }

    /**
     * Fügt die übergebene UserGroup der Liste von UserGroups hinzu.
     *
     * @param userGroup
     */
    public boolean addListItem(UserGroup userGroup) {
        if (maximum > -1 && groupList.size() >= maximum) {
            Toast.makeText(context, R.string.usergroup_adapter_maximum_reached, Toast.LENGTH_LONG).show();
            return false;
        }
        groupList.add(userGroup);
        notifyDataSetChanged();
        return true;
    }
}
