package ws1415.veranstalterapp.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.skatenight.skatenightAPI.model.Event;

import java.util.List;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.common.task.QueryEventsTask;
import ws1415.veranstalterapp.dialog.AddRouteDialog;
import ws1415.veranstalterapp.adapter.TabsPagerAdapter;
import ws1415.veranstalterapp.fragment.ShowEventsFragment;
import ws1415.veranstalterapp.R;

/**
 * Die Activity, welche beim Starten der Veranstalter App ausgeführt wird. Diese Activity
 * dient zum Halten von Fragmenten, dem ShowInformationFragment, AnnounceInformationFragment und dem
 * ManageRoutesFragment.
 *
 * @author Bernd Eissing, Martin Wrodarczyk
 */
public class HoldTabsActivity extends FragmentActivity implements ActionBar.TabListener {
    private static ViewPager viewPager;
    private static TabsPagerAdapter mAdapter;
    private static ActionBar actionBar;

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
        getMenuInflater().inflate(R.menu.hold_tabs, menu);
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
        if(id == R.id.action_add_route) {
            Intent intent = new Intent(this, AddRouteDialog.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.action_permission){
            Intent intent = new Intent(this, PermissionManagementActivity.class);
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
        new QueryEventsTask(new ExtendedTaskDelegateAdapter<Void, List<Event>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<Event> events) {
                ((ShowEventsFragment) mAdapter.getItem(0)).setEventsToListView(events);
            }
        }).execute();
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
