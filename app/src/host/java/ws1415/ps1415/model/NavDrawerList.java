package ws1415.ps1415.model;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import ws1415.ps1415.activity.ListUserGroupsActivity;
import ws1415.ps1415.activity.PermissionManagementActivity;
import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.FriendsActivity;
import ws1415.ps1415.activity.ListEventsActivity;
import ws1415.ps1415.activity.ManageEventsActivity;
import ws1415.ps1415.activity.ManageRoutesActivity;
import ws1415.ps1415.activity.MessagingActivity;
import ws1415.ps1415.activity.MyPicturesActivity;
import ws1415.ps1415.activity.PermissionManagementActivity;
import ws1415.ps1415.activity.ProfileActivity;
import ws1415.ps1415.activity.RegisterActivity;
import ws1415.ps1415.activity.SearchActivity;
import ws1415.ps1415.util.PrefManager;

/**
 * @author Richard Schulze
 */
public class NavDrawerList {
    public static final NavDrawerItem[] items = new NavDrawerItem[] {
            // ---------- Veranstaltungen ----------
            new NavDrawerItem() {
                @Override
                public int getTitleId() {
                    return R.string.events;
                }
                @Override
                public int getIconId() {
                    return R.drawable.ic_event;
                }
                @Override
                public void onClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent show_events_intent = new Intent(parent.getContext(), ListEventsActivity.class);
                    show_events_intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    parent.getContext().startActivity(show_events_intent);
                }
            },

            // ---------- Mein Profil ----------
            new NavDrawerItem() {
                @Override
                public int getTitleId() {
                    return R.string.my_profile;
                }
                @Override
                public int getIconId() {
                    return R.drawable.ic_action_person;
                }
                @Override
                public void onClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent profile_intent = new Intent(parent.getContext(), ProfileActivity.class);
                    profile_intent.putExtra("email", ServiceProvider.getEmail());
                    parent.getContext().startActivity(profile_intent);
                }
            },

            // ---------- Messaging ----------
            new NavDrawerItem() {
                @Override
                public int getTitleId() {
                    return R.string.messages;
                }
                @Override
                public int getIconId() {
                    return R.drawable.ic_action_chat;
                }
                @Override
                public void onClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent messaging_intent = new Intent(parent.getContext(), MessagingActivity.class);
                    parent.getContext().startActivity(messaging_intent);
                }
            },

            // ---------- Meine Bilder ----------
            new NavDrawerItem() {
                @Override
                public int getTitleId() {
                    return R.string.my_pictures;
                }
                @Override
                public int getIconId() {
                    return R.drawable.ic_action_picture;
                }
                @Override
                public void onClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(parent.getContext(), MyPicturesActivity.class);
                    parent.getContext().startActivity(intent);
                }
            },

            // ---------- Freunde ----------
            new NavDrawerItem() {
                @Override
                public int getTitleId() {
                    return R.string.friends;
                }
                @Override
                public int getIconId() {
                    return R.drawable.ic_group;
                }
                @Override
                public void onClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent friends_intent = new Intent(parent.getContext(), FriendsActivity.class);
                    parent.getContext().startActivity(friends_intent);
                }
            },

            // ---------- Gruppen ----------
            new NavDrawerItem() {
                @Override
                public int getTitleId() {
                    return R.string.user_groups;
                }
                @Override
                public int getIconId() {
                    return R.drawable.ic_group;
                }
                @Override
                public void onClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent user_group_intent = new Intent(parent.getContext(), ListUserGroupsActivity.class);
                    user_group_intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    parent.getContext().startActivity(user_group_intent);
                }
            },
            
            // ---------- Suche ----------
            new NavDrawerItem() {
                @Override
                public int getTitleId() {
                    return R.string.search;
                }
                @Override
                public int getIconId() {
                    return R.drawable.ic_action_search;
                }
                @Override
                public void onClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent search_intent = new Intent(parent.getContext(), SearchActivity.class);
                    search_intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    parent.getContext().startActivity(search_intent);
                }
            },

            // ---------------------------------------------
            // ---------- Veranstalter-Funktionen ----------
            // ---------------------------------------------

            // ---------- Veranstaltung erstellen/bearbeiten ----------
            new NavDrawerItem() {
                @Override
                public int getTitleId() {
                    return R.string.create_edit_events;
                }
                @Override
                public int getIconId() {
                    return R.drawable.ic_event;
                }
                @Override
                public void onClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(parent.getContext(), ManageEventsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    parent.getContext().startActivity(intent);
                }
            },

            // ---------- Routen erstellen/bearbeiten ----------
            new NavDrawerItem() {
                @Override
                public int getTitleId() {
                    return R.string.create_edit_routes;
                }
                @Override
                // TODO Icon ändern
                public int getIconId() {
                    return R.drawable.ic_event;
                }
                @Override
                public void onClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(parent.getContext(), ManageRoutesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    parent.getContext().startActivity(intent);
                }
            },

            // ---------- Rechteverwaltung ----------
            new NavDrawerItem() {
                @Override
                public int getTitleId() {
                    return R.string.permission_management;
                }

                @Override
                public int getIconId() {
                    return R.drawable.ic_action_accounts;
                }

                @Override
                public void onClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent admins_intent = new Intent(parent.getContext(), PermissionManagementActivity.class);
                    parent.getContext().startActivity(admins_intent);
                }
            },

            // ---------- Logout ----------
            new NavDrawerItem() {
                @Override
                public int getTitleId() {
                    return R.string.logout;
                }

                @Override
                public int getIconId() {
                    return R.drawable.ic_action_accounts;
                }

                @Override
                public void onClick(AdapterView<?> parent, View view, int position, long id) {
                    PrefManager.setSelectedUserMail(parent.getContext(), "");
                    Intent intent = new Intent(parent.getContext(), RegisterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    parent.getContext().startActivity(intent);
                }
            }
    };
}
