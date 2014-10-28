package ws1415.veranstalterapp.Adaper;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;
import java.util.ArrayList;
import java.util.List;

import ws1415.veranstalterapp.AnnounceInformationFragment;
import ws1415.veranstalterapp.ManageRoutesFragment;
import ws1415.veranstalterapp.ShowInformationFragment;

/**
 * Created by Bernd Eissing on 28.10.2014.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter{
    private List<Fragment> myTabs = new ArrayList<Fragment>();

    public TabsPagerAdapter(FragmentManager fm){
        super(fm);
        myTabs.add(new ShowInformationFragment());
        myTabs.add(new AnnounceInformationFragment());
        myTabs.add(new ManageRoutesFragment());
    }

    @Override
    public Fragment getItem(int index){

        switch(index){
            case 0:
                // ShowInformationFragment
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

    @Override
    public int getCount(){
        // Gibt die Anzahl an Tabs zurück
        return myTabs.size();
    }
}
