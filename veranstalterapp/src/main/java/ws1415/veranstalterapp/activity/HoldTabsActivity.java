package ws1415.veranstalterapp.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import ws1415.veranstalterapp.adaptertest.TabsPagerAdapter;
import ws1415.veranstalterapp.AddRouteDialog;
import ws1415.veranstalterapp.fragment.ShowEventsFragment;
import ws1415.veranstalterapp.R;
import ws1415.veranstalterapp.task.QueryEventTask;

/**
 * Die Activity, welche beim starten der Veranstalter App ausgeführt wird. Diese Activity
 * dient zum halten von Fragmenten, dem ShowInformationFragment, AnnounceInformationFragment und dem
 * ManageRoutesFragment.
 *
 * Created by Bernd Eissing, Martin Wrodarczyk.
 */
public class HoldTabsActivity extends FragmentActivity implements ActionBar.TabListener {
    private static ViewPager viewPager;
    private static TabsPagerAdapter mAdapter;
    private static ActionBar actionBar;
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
        setContentView(R.layout.activity_hold_tabs);


        // Beschrifte die Tabs
        tabs = new String[]{
                getResources().getString(R.string.title_fragment_show_events),
                getResources().getString(R.string.title_fragment_announce_information),
                getResources().getString(R.string.title_fragment_manage_routes)};

        // ViewPager initialisieren
        viewPager = (ViewPager) findViewById(R.id.pager);

        // Adapter initialisieren
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        // Action Bar initialisieren
        actionBar = getActionBar();

        // Adapter setzen und in der Aciton Bar die Tabs setzen
        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
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
    }

    /**
     * Erstellt das ActionBar Menu.
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hold_tabs, menu);
        menuItem = menu.findItem(R.id.action_add_route);
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_add_route) {
            Intent intent = new Intent(this, AddRouteDialog.class);
            startActivity(intent);
            return true;
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
     * Updated die Informationen in dem Tab "ShowEventsFragment", diese Methode wird aufgerufen,
     * wenn die Informationen im "AnnounceInformationFragment" gesetzt und abgeschickt werden.
     */
    public static void updateInformation(){
        new QueryEventTask().execute((ShowEventsFragment) mAdapter.getItem(0));
    }

    /**
     * Gibt den Adapter zurück, der die Tabs verwaltet.
     *
     * @return Der Adapter
     */
    public static TabsPagerAdapter getAdapter(){
        return mAdapter;
    }

    /**
     * Gibt den ViewPager zurück.
     *
     * @return
     */
    public static ViewPager getViewPager(){
        return viewPager;
    }
}
