package ws1415.ps1415.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import ws1415.ps1415.adapter.TabsUsergroupsAdapter;
//import ws1415.ps1415.fragment.AllUsergroupsFragment;
import ws1415.ps1415.R;
//import ws1415.ps1415.fragment.MyUsergroupsFragment;

/**
 * Diese Activity wird zum verwalten der Nutzergruppen genutzt. Sie zeigt das All- und
 * MyUserGroupsFragment an.
 *
 * @author Bernd Eissing, Martin Wrodarczyk
 */
public class UsergroupActivity extends BaseFragmentActivity implements ActionBar.TabListener {
    public static final String REFRESH_GROUPS_ACTION = "REFRESH_GROUPS";

    private static ViewPager viewPager;
    private static TabsUsergroupsAdapter mAdapter;
    private static ActionBar actionBar;
    private static UsergroupActivity mActivity;
    private MenuItem menuItem;
    private boolean menuCreated;

    private String[] tabs;

    /**
     * Erstellt die Activity, beschriftet die Tabs und initialisiert die Tabverwaltung.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_group);

        mActivity = this;

        // Beschrifte die Tabs
        tabs = new String[]{
                getResources().getString(R.string.title_fragment_all_user_groups),
                getResources().getString(R.string.title_fragment_my_user_groups)};

        // ViewPager initialisieren
        viewPager = (ViewPager) findViewById(R.id.pager_user_group);

        // Adapter initialisieren
        mAdapter = new TabsUsergroupsAdapter(getSupportFragmentManager());

        // Action Bar initialisieren
        actionBar = getActionBar();

        // Adapter setzen und in der Aciton Bar die Tabs setzen
        viewPager.setAdapter(mAdapter);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Tabs setzen
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(this));
        }

        // Setze akuellen Tab beim swipen
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            /**
             * Wechselt den Tab, wenn die Seite beim Swipen verändert wird.
             *
             * @param position Die Position des Tabs, zu dem geswiped wird
             */
            @Override
            public void onPageSelected(int position) {
                // Wechsel Tab, wenn Seite beim Swipen verändert wird.
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2){

            }

            @Override
            public void onPageScrollStateChanged(int arg0){

            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
               // refresh();
            }
        }, new IntentFilter(REFRESH_GROUPS_ACTION));
    }

    /**
     * Refresht die Liste aller Gruppen und eigener Gruppen.
     */
//    public void refresh(){
//        AllUsergroupsFragment allUsergroupsFragment = (AllUsergroupsFragment)mAdapter.getItem(0);
//        MyUsergroupsFragment myUsergroupsFragment = (MyUsergroupsFragment)mAdapter.getItem(1);
//        if(allUsergroupsFragment != null) allUsergroupsFragment.refresh();
//        if(myUsergroupsFragment != null) myUsergroupsFragment.refresh();
//    }

    /**
     * Gibt das Activity Objekt zurück
     */
    public static UsergroupActivity getUserGroupActivity(){
        return mActivity;
    }

    /**
     * Erstellt das ActionBar Menu.
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_user_group, menu);
        menuItem = menu.findItem(R.id.action_add_user_group);
        return true;
    }

    /**
     * Händelt ActionBar Item clicks. Wenn man das action_add_route Item
     * auswählt, wird die RouteEditorActivity gestartet.
     *
     * @param item Das Item in der Action Bar
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        if(id == R.id.action_add_user_group) {
            Intent intent = new Intent(this, AddUserGroupActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(UsergroupActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if(id == R.id.action_refresh_groups) {
            //refresh();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Wechselt Tab bei Tabauswahl
     *
     * @param tab Ausgewählter Tab
     * @param fragmentTransaction
     */
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // Zeige aktuell gewählten Tab an
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    /**
     * Gibt den Adapter zurück, der die Tabs verwaltet.
     *
     * @return Der Adapter
     */
    public static TabsUsergroupsAdapter getAdapter(){
        return mAdapter;
    }


    /**
     * @return das MenuItem
     */
    public MenuItem getMenuItem(){
        return menuItem;
    }
}
