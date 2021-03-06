package ws1415.veranstalterapp.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import ws1415.veranstalterapp.fragment.AnnounceInformationFragment;
import ws1415.veranstalterapp.fragment.ManageRoutesFragment;
import ws1415.veranstalterapp.fragment.ShowEventsFragment;

/**
 * Klasse die die Tabs der HoldTabsActivity verwaltet.
 *
 * Created by Bernd Eissing, Martin Wrodarczyk on 28.10.2014.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter{
    private List<Fragment> myTabs = new ArrayList<Fragment>();

    /**
     * Konstruktor, der die Tabs initialisiert.
     *
     * @param fm der FragmentManager
     */
    public TabsPagerAdapter(FragmentManager fm){
        super(fm);
        myTabs.add(new ShowEventsFragment());
        myTabs.add(new AnnounceInformationFragment());
        myTabs.add(new ManageRoutesFragment());
    }

    /**
     * Gibt das Fragment, welches im Tab an der Stelle Index gespeichert ist, zurück
     *
     * @param index Stelle des Fragments, 0 ShowEventsFragment, 1 AnnounceInformationFragment, 2 ManageRoutesFragment
     * @return das Fragment
     */
    @Override
    public Fragment getItem(int index){

        switch(index){
            case 0:
                // ShowEventsFragment
                return myTabs.get(0);
            case 1:
                // AnnounceInformationFragment
                return myTabs.get(1);
            case 2:
                // ManageRoutesFragment
                return myTabs.get(2);
        }
        return null;
    }

    /**
     * Gibt die Anzahl an Tabs zurück
     *
     * @return Anz. der Tabs
     */
    @Override
    public int getCount(){
        // Gibt die Anzahl an Tabs zurück
        return myTabs.size();
    }
}
