package ws1415.ps1415.model;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.FriendsActivity;
import ws1415.ps1415.activity.ListEventsActivity;
import ws1415.ps1415.activity.MessagingActivity;
import ws1415.ps1415.activity.ProfileActivity;
import ws1415.ps1415.activity.SearchActivity;
import ws1415.ps1415.activity.UploadImageActivity;
import ws1415.ps1415.activity.UsergroupActivity;

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
                    Intent user_group_intent = new Intent(parent.getContext(), UsergroupActivity.class);
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



            // ---------- Testeinträge ----------
            new NavDrawerItem() {
                @Override
                public int getTitleId() {
                    return R.string.upload_group_image;
                }
                @Override
                public int getIconId() {
                    return R.drawable.ic_action_new;
                }
                @Override
                public void onClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent upload_group_picture_intent = new Intent(parent.getContext(), UploadImageActivity.class);
                    upload_group_picture_intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    parent.getContext().startActivity(upload_group_picture_intent);
                }
            }
    };
}
