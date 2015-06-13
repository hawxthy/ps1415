package ws1415.ps1415.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupFilter;
import com.skatenight.skatenightAPI.model.UserGroupMetaData;

import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.activity.ListUserGroupsActivity;
import ws1415.ps1415.activity.MyUserGroupsActivity;
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
    private final int LOAD_BUFFER = 5;
    private List<UserGroupMetaData> groupList;
    private Context context;
    private LayoutInflater inflater;
    // Attribut zum speichern, ob gerade Gruppen geladen werden
    private boolean fetching = false;
    private boolean moreToFetch = true;
    private UserGroupFilter filter;
    private List<String> visibleGroupCount;
    // String zum Suchen von Nutzergruppen
    private String mSearchString;
    private List<String> mSearchGroups;

    /**
     * Konstruktor, der benutzt werden kann, falls man nach einem String
     * suchen möchte. Der Unterschied hier ist, dass nach Gruppennamen gesucht
     * wird, die mit dem searchString anfangen.
     *
     * @param context      Context, von dem aus der Adapter aufgerufen wird.
     * @param searchString String nach dem gesucht werden soll kann null sein
     */
    public UsergroupAdapter(Context context, String searchString, UserGroupFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("no filter submitted");
        }
        this.mSearchString = searchString;
        this.mSearchGroups = new ArrayList<>();
        this.context = context;
        this.filter = filter;
        this.groupList = new ArrayList<>();
        this.visibleGroupCount = new ArrayList<>();
        if (context != null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        fetchNewGroups();
    }

    /**
     * Konstruktor, der benutzt werden kann falls man genau nach Gruppen suchen will.
     * Der Unterschied ist hier, dass in searchGroups Gruppennamen enthalten sein müssen.
     * Ist dies nicht der Fall, so wird auch keine Gruppe dazu geladen.
     *
     * @param context      Context, von dem aus der Adapter aufgerufen wird.
     * @param searchGroups Strings nach denen gesucht werden soll kann null sein
     */
    public UsergroupAdapter(Context context, UserGroupFilter filter, List<String> searchGroups) {
        if (filter == null) {
            throw new IllegalArgumentException("no filter submitted");
        }
        this.mSearchString = null;
        this.mSearchGroups = searchGroups;
        this.context = context;
        this.filter = filter;
        this.visibleGroupCount = new ArrayList<>();
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
    public UserGroupMetaData getItem(int i) {
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
        if (position == groupList.size() - LOAD_BUFFER && !fetching && moreToFetch)
            fetchNewGroups();

        if (convertView == null) {
            holder = new Holder();
            convertView = inflater.inflate(R.layout.list_view_item_usergroup, viewGroup, false);
            holder.groupImage = (ImageView) convertView.findViewById(R.id.user_group_list_view_item_image);
            holder.groupName = (TextView) convertView.findViewById(R.id.user_group_list_view_item_title);
            holder.groupCount = (TextView) convertView.findViewById(R.id.user_group_list_view_item_count);
            holder.hiddenSecurityButton = (ImageView) convertView.findViewById(R.id.hidden_security_group_button);
            holder.hiddenCancelButton = (ImageView) convertView.findViewById(R.id.hidden_cancel_button);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        // Image Laden

        GroupImageLoader.getInstance().setGroupImageToImageView(context, getItem(position).getBlobKey(), holder.groupImage);

        //Attribute Setzen
        holder.groupName.setText(getItem(position).getName());
        holder.groupCount.setText(context.getString(R.string.usergroup_member_count) + Integer.toString(getItem(position).getMemberCount()));

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
            } else {
                holder.hiddenSecurityButton.setVisibility(View.GONE);
            }
        } else if(context instanceof MyUserGroupsActivity){
            if (visibleGroupCount.size() < 6) {
                String groupName = getItem(position).getName();
                final MyUserGroupsActivity activity = (MyUserGroupsActivity) context;
                //Setzen der Icons
                holder.hiddenCancelButton.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_eye_off_black_24dp));
                holder.hiddenSecurityButton.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_eye_black_24dp));
                // Prüfe auf Sichtbarkeit und mache die Buttons dementsprechend sichtbar
                if (PrefManager.getGroupVisibility(context, groupName)) {
                    if(!visibleGroupCount.contains(groupName)){
                        visibleGroupCount.add(groupName);
                    }
                    holder.hiddenCancelButton.setVisibility(View.VISIBLE);
                    holder.hiddenSecurityButton.setVisibility(View.GONE);
                }else{
                    holder.hiddenSecurityButton.setVisibility(View.VISIBLE);
                    holder.hiddenCancelButton.setVisibility(View.GONE);
                }

                // Setze die Listener
                holder.hiddenSecurityButton.setOnClickListener(new View.OnClickListener() {
                    String groupName = getItem(position).getName();

                    @Override
                    public void onClick(View view) {
                        if (!PrefManager.getGroupVisibility(context, groupName) && visibleGroupCount.size() < 5) {
                            holder.hiddenCancelButton.setVisibility(View.VISIBLE);
                            holder.hiddenSecurityButton.setVisibility(View.GONE);
                            PrefManager.setGroupVisibility(context, groupName, true);
                            if(!visibleGroupCount.contains(groupName)){
                                visibleGroupCount.add(groupName);
                            }
                        }else{
                            Toast.makeText(context, R.string.too_many_visible_groups, Toast.LENGTH_LONG).show();
                        }
                    }
                });
                holder.hiddenCancelButton.setOnClickListener(new View.OnClickListener() {
                    String groupName = getItem(position).getName();

                    @Override
                    public void onClick(View view) {
                        if (PrefManager.getGroupVisibility(context, groupName)) {
                            holder.hiddenCancelButton.setVisibility(View.GONE);
                            holder.hiddenSecurityButton.setVisibility(View.VISIBLE);
                            PrefManager.setGroupVisibility(context, groupName, false);
                            if(visibleGroupCount.contains(groupName)){
                                visibleGroupCount.remove(groupName);
                            }
                        }
                    }
                });
            } else {
                Toast.makeText(context, R.string.tooManyVisibleGroups, Toast.LENGTH_LONG).show();
            }
        }else{
            // Setze keine hidden Buttons. Dies passiert, wenn eine andere Activity oder Fragment
            // als die MyUserGroupsActivity und ListUserGroupsActivity diesen Adapter benutzen
        }


        return convertView;
    }


    /**
     * Ruft neue Nutzergruppen ab, wenn aufgerufen und im moment noch keine
     * abgerufen werden.
     */
    protected void fetchNewGroups() {
        if (context instanceof MyUserGroupsActivity) {
            if (!fetching) {
                final Activity activity = (Activity) context;
                activity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
                GroupController.getInstance().getMyUserGroups(new ExtendedTaskDelegateAdapter<Void, List<UserGroupMetaData>>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, List<UserGroupMetaData> metaDatas) {
                        if (metaDatas == null) {
                            Toast.makeText(context, R.string.noMore, Toast.LENGTH_LONG).show();
                        } else {
                            groupList.addAll(metaDatas);
                            notifyDataSetChanged();

                            // Falls das Ende erreicht is, muss der Adapter bescheid bekommen
                            if (metaDatas.size() < filter.getLimit()) {
                                moreToFetch = false;
                            }
                        }
                        fetching = false;
                        activity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                    }

                    @Override
                    public void taskFailed(ExtendedTask task, String message) {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        activity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                    }
                }, filter);
            }
        } else {
            if (!fetching) {
                final Activity activity = (Activity) context;
                activity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
                fetching = true;
                if (mSearchString != null) {
                    GroupController.getInstance().searchUserGroups(new ExtendedTaskDelegateAdapter<Void, List<UserGroupMetaData>>() {
                        @Override
                        public void taskDidFinish(ExtendedTask task, List<UserGroupMetaData> metaDatas) {
                            if (metaDatas == null) {
                                Toast.makeText(context, R.string.noMore, Toast.LENGTH_LONG).show();
                            } else {
                                groupList.addAll(metaDatas);
                                notifyDataSetChanged();

                                //Falls das Ende erreicht ist, muss der Adapter bescheid bekommen
                                if (metaDatas.size() < filter.getLimit()) {
                                    moreToFetch = false;
                                }
                            }
                            fetching = false;
                            activity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        }
                    }, mSearchString, filter);
                }else if(mSearchGroups != null && mSearchGroups.size() > 0){
                    final List<String> subList = mSearchGroups.subList(0, Math.min(mSearchGroups.size(), filter.getLimit()));
                    GroupController.getInstance().fetchSpecificGroupDatas(new ExtendedTaskDelegateAdapter<Void, List<UserGroupMetaData>>(){
                        @Override
                        public void taskDidFinish(ExtendedTask task, List<UserGroupMetaData> metaDatas) {
                            if (metaDatas != null) {
                                mSearchGroups.removeAll(subList);
                                groupList.addAll(metaDatas);
                                notifyDataSetChanged();

                                //Falls das Ende erreicht ist, muss der Adapter bescheid bekommen
                                if (metaDatas.size() < filter.getLimit()) {
                                    moreToFetch = false;
                                }
                            }

                            fetching = false;
                            activity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        }
                    }, subList);
                } else {
                    GroupController.getInstance().listAllUserGroupMetaDatas(new ExtendedTaskDelegateAdapter<Void, List<UserGroupMetaData>>() {
                        @Override
                        public void taskDidFinish(ExtendedTask task, List<UserGroupMetaData> metaDatas) {
                            if (metaDatas == null) {
                                Toast.makeText(context, R.string.noMore, Toast.LENGTH_LONG).show();
                            } else {
                                groupList.addAll(metaDatas);
                                notifyDataSetChanged();

                                //Falls das Ende erreicht ist, muss der Adapter bescheid bekommen
                                if (metaDatas.size() < filter.getLimit()) {
                                    moreToFetch = false;
                                }
                            }
                            fetching = false;
                            activity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                            activity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        }
                    }, filter);
                }
            }
        }
    }

    /**
     * Übergibt dem Filter den cursorstring, damit beim nächsten Aufruf von
     * listAllUserGroupMetaDatas der alte Cursor verwendet wird.
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
}
