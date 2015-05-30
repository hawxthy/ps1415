package ws1415.ps1415.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

import ws1415.common.net.ServiceProvider;
import ws1415.ps1415.R;
import ws1415.ps1415.adapter.NavDrawerListAdapter;
import ws1415.ps1415.model.NavDrawerItem;

/**
 * Diese Activity ist die Oberklasse von allen Activities die einen Navigation Drawer enthalten.
 *
 * @author Martin Wrodarczyk
 */
public class BaseActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    protected LinearLayout fullLayout;
    protected FrameLayout actContent;

    /**
     * Setzt den Navigation Drawer(fullLayout) und erwartet eine layout Id die im eigentlichen
     * Content gesetzt wird.
     *
     * @param layoutResID
     */
    @Override
    public void setContentView(final int layoutResID){
        fullLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        actContent = (FrameLayout) fullLayout.findViewById(R.id.act_content);

        getLayoutInflater().inflate(layoutResID, actContent,  true);
        super.setContentView(fullLayout);

        //Slide Menu Items laden
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        //Nav Drawer Icons initialisieren
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // Die Nav Drawer Items hinzufügen
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));

        navMenuIcons.recycle();

        // Den Nav Drawer Adapter setzen
        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);

        // Action Bar Item aktivieren
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.app_name,
                R.string.app_name
        ){
            public void onDrawerClosed(View view) {
                for(int i=0; i < navDrawerItems.size(); i++) mDrawerList.setItemChecked(i, false);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
    }

    /**
     * Slide menu item click listener
     * */
    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

        /** Swaps fragments in the main content view */
        private void selectItem(int position) {
            switch(position){
                case 0:
                    Intent profile_intent = new Intent(BaseActivity.this, ProfileActivity.class);
                    profile_intent.putExtra("email", ServiceProvider.getEmail());
                    startActivity(profile_intent);
                    break;
                case 1:
                    Intent show_events_intent = new Intent(BaseActivity.this, ListEventsActivity.class);
                    show_events_intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(show_events_intent);
                    break;
                case 2:
                    Intent user_group_intent = new Intent(BaseActivity.this, UsergroupActivity.class);
                    user_group_intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(user_group_intent);
                    break;
                case 3:
                    Intent cloud_storage_test_intent = new Intent(BaseActivity.this, ImageStorageTestActivity.class);
                    cloud_storage_test_intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(cloud_storage_test_intent);
                    break;
                case 4:
                    Intent search_intent = new Intent(BaseActivity.this, SearchActivity.class);
                    search_intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(search_intent);
                    break;
            }
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(BaseActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

}
