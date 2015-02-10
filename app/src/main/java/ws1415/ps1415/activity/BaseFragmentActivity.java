package ws1415.ps1415.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.NavDrawerListAdapter;
import ws1415.ps1415.model.NavDrawerItem;

public class BaseFragmentActivity extends FragmentActivity {
    // NavigationDrawer
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    protected LinearLayout fullLayout;
    protected FrameLayout actContent;

    @Override
    public void setContentView(final int layoutResID){
        fullLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        actContent = (FrameLayout) fullLayout.findViewById(R.id.act_content);

        getLayoutInflater().inflate(layoutResID, actContent,  true);
        super.setContentView(fullLayout);

        // NavigationDrawer initialisieren
        mTitle = mDrawerTitle = getTitle();

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
                for(int i=0; i<navDrawerItems.size(); i++) mDrawerList.setItemChecked(0, false);
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
                    Intent show_events_intent = new Intent(BaseFragmentActivity.this, ShowEventsActivity.class);
                    startActivity(show_events_intent);
                    break;
                case 1:
                    Intent user_group_intent = new Intent(BaseFragmentActivity.this, UsergroupActivity.class);
                    startActivity(user_group_intent);
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
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
