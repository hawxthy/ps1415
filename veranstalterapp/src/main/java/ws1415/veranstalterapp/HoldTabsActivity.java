package ws1415.veranstalterapp;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import ws1415.veranstalterapp.Adaper.TabsPagerAdapter;


public class HoldTabsActivity extends FragmentActivity implements ActionBar.TabListener {
    private ViewPager viewPager;
    private static TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    private MenuItem menuItem;
    private boolean menuCreated;

    private String[] tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hold_tabs);


        // Beschrifte die Tabs
        tabs = new String[]{
                getResources().getString(R.string.title_fragment_show_information),
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hold_tabs, menu);
        menuItem = menu.findItem(R.id.action_add_route);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_add_route){
            // TODO Methode addRoute() muss hier aufgerufen werden
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        // Zeige aktuell gewählten Tab an
        viewPager.setCurrentItem(tab.getPosition());
        if(tab.getPosition()== 2){
            //menuItem.setVisible(true);
        } else{
            //menuItem.setVisible(false);
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    public static void updateInformation(){
        new QueryEventTask().execute((ShowInformationFragment) mAdapter.getItem(0));
    }
}
