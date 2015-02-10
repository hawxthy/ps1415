package ws1415.ps1415.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import ws1415.ps1415.R;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
