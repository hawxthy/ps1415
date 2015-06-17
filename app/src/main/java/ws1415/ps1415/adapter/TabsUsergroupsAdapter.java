package ws1415.ps1415.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Dieser Adapter wird genutzt, um die Fragmente der GroupProfileActivity zu setzen.
 *
 * @author Bernd Eissing
 */
public class TabsUsergroupsAdapter extends FragmentPagerAdapter {
    private List<Fragment> myTabs = new ArrayList<>();

    /**
     * Konstruktor, der die Tabs initialisiert.
     *
     * @param fm der FragmentManager
     */
    public TabsUsergroupsAdapter(FragmentManager fm){
        super(fm);
        //myTabs.add(new AllUsergroupsFragment());
        //myTabs.add(new MyUsergroupsFragment());
    }

    /**
     * Gibt das Fragment, welches im Tab an der Stelle "index" gespeichert ist, zurück.
     *
     * @param index Stelle des Fragments, 0 AllUsergroupsFragment, 1 MyUsergroupsFragment()
     * @return das Fragment
     */
    @Override
    public Fragment getItem(int index){
        switch(index){
            case 0:
                return myTabs.get(0);
            case 1:
                return myTabs.get(1);
        }
        return null;
    }

    /**
     * Gibt die Anzahl an Tabs zurück.
     *
     * @return Anzahl der Tabs
     */
    @Override
    public int getCount(){
        return myTabs.size();
    }

}
