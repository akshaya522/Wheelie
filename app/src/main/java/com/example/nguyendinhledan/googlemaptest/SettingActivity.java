package com.example.nguyendinhledan.googlemaptest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private SeekBarPreference _seekBarPref;
        private Context mContext;

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);

            mContext = getActivity().getApplicationContext();

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);


            // Get widgets :
            _seekBarPref = (SeekBarPreference) this.findPreference("SEEKBAR_VALUE");

            // Set listener :
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            // Set seekbar summary :
            int radius = PreferenceManager.getDefaultSharedPreferences(mContext).getInt("SEEKBAR_VALUE", 500);
            _seekBarPref.setSummary(this.getString(R.string.settings_radius_summary).replace("$1", ""+radius));
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            // Set seekbar summary :
            int radius = PreferenceManager.getDefaultSharedPreferences(mContext).getInt("SEEKBAR_VALUE", 500);
            _seekBarPref.setSummary(this.getString(R.string.settings_radius_summary).replace("$1", ""+radius));
        }
    }
}
