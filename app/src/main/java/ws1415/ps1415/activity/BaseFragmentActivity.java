package ws1415.ps1415.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.adapter.NavDrawerListAdapter;
import ws1415.ps1415.model.NavDrawerItem;
import ws1415.ps1415.model.NavDrawerList;
import ws1415.ps1415.util.UniversalUtil;

/**
 * Diese Activity ist die Oberklasse von allen Activities die einen Navigation Drawer enthalten.
 *
 * @author Martin Wrodarczyk
 */
public class BaseFragmentActivity extends FragmentActivity {
    private static boolean GCM_INITIALIZED = false;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    protected LinearLayout fullLayout;
    protected FrameLayout actContent;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(!GCM_INITIALIZED && ServiceProvider.getEmail() != null) {
            UniversalUtil.initGCM(this);
            GCM_INITIALIZED = true;
        }
    }

    public void setContentView(NavDrawerItem[] items, final int layoutResID) {
        fullLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        actContent = (FrameLayout) fullLayout.findViewById(R.id.act_content);

        getLayoutInflater().inflate(layoutResID, actContent, true);
        super.setContentView(fullLayout);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // Die Nav Drawer Items hinzufügen
        navDrawerItems.addAll(Arrays.asList(items));

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
     * Setzt den Navigation Drawer(fullLayout) und erwartet eine layout Id die im eigentlichen
     * Content gesetzt wird.
     *
     * @param layoutResID
     */
    @Override
    public void setContentView(final int layoutResID) {
        setContentView(NavDrawerList.items, layoutResID);
    }

    /**
     * Slide menu item click listener
     * */
    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mDrawerLayout.closeDrawer(mDrawerList);
            navDrawerItems.get(position).onClick(parent, view, position, id);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
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
