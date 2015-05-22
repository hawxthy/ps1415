package ws1415.ps1415.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.fragment.ProfileEventsFragment;
import ws1415.ps1415.fragment.ProfileGroupsFragment;
import ws1415.ps1415.fragment.ProfileInfoFragment;

/**
 * Created by Martin on 21.05.2015.
 */
public class ProfilePagerAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> myTabs = new ArrayList<Fragment>();
    String titles[];

    public ProfilePagerAdapter(FragmentManager fm, String[] titles) {
        super(fm);
        this.titles = titles;
        myTabs.add(new ProfileGroupsFragment());
        myTabs.add(new ProfileInfoFragment());
        myTabs.add(new ProfileEventsFragment());
    }

    public ProfileGroupsFragment getGroupFragment(){
        return (ProfileGroupsFragment) myTabs.get(0);
    }

    public ProfileInfoFragment getInfoFragment(){
        return (ProfileInfoFragment) myTabs.get(1);
    }

    public ProfileEventsFragment getEventFragment(){
        return (ProfileEventsFragment) myTabs.get(2);
    }

    @Override
    public Fragment getItem(int position) {
        return myTabs.get(position);
    }

    @Override
    public String getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public int getCount() {
        return myTabs.size();
    }
}
