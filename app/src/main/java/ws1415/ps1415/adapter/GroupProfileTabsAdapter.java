package ws1415.ps1415.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.skatenight.skatenightAPI.model.UserGroupMembers;

import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.fragment.GroupBlackBoardFragment;
import ws1415.ps1415.fragment.GroupMembersFragment;
import ws1415.ps1415.fragment.GroupNewsBoardFragment;

/**
 * Dieser Adapter wird genutzt, um die Fragmente der GroupProfileActivity zu setzen.
 *
 * @author Bernd Eissing
 */
public class GroupProfileTabsAdapter extends FragmentPagerAdapter {
    private List<Fragment> myTabs = new ArrayList<>();
    private String tabTitles[];

    /**
     * Konstruktor, der die Tabs initialisiert.
     *
     * @param fm der FragmentManager
     */
    public GroupProfileTabsAdapter(FragmentManager fm, String[] titles) {
        super(fm);
        tabTitles = titles;
        myTabs.add(new GroupMembersFragment());
        myTabs.add(new GroupBlackBoardFragment());
        myTabs.add(new GroupNewsBoardFragment());
    }

    /**
     * Gibt das Fragment, welches im Tab an der Stelle "index" gespeichert ist, zurück.
     *
     * @param index Stelle des Fragments, 0 AllUsergroupsFragment, 1 MyUsergroupsFragment()
     * @return das Fragment
     */
    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                return myTabs.get(0);
            case 1:
                return myTabs.get(1);
            case 2:
                return myTabs.get(2);
        }
        return null;
    }

    /**
     * Gibt die Anzahl an Tabs zurück.
     *
     * @return Anzahl der Tabs
     */
    @Override
    public int getCount() {
        return myTabs.size();
    }

    @Override
    public String getPageTitle(int position) {
        return tabTitles[position];
    }

    public GroupMembersFragment getGroupMembersFragment(){
        return (GroupMembersFragment)myTabs.get(0);
    }

    public GroupBlackBoardFragment getGroupBlackBoardFragment(){
        return (GroupBlackBoardFragment)myTabs.get(1);
    }
    public GroupNewsBoardFragment getGroupNewsBoardFragment(){
        return (GroupNewsBoardFragment)myTabs.get(2);
    }
}

