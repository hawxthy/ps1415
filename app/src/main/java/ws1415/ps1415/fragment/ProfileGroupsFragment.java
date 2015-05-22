package ws1415.ps1415.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ws1415.ps1415.R;

/**
 * Created by Martin on 21.05.2015.
 */
public class ProfileGroupsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_profile_groups, container, false);

        return rootView;
    }
}
