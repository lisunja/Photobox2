package com.example.photobox2.view;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.viewmodel.CreationExtras;
import androidx.preference.PreferenceFragmentCompat;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.photobox2.R;
import com.example.photobox2.database.SettingsDatabaseManager;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
            String value = sharedPreferences.getString(key, "");
            saveSetting(key, value);
        }

        @NonNull
        @Override
        public CreationExtras getDefaultViewModelCreationExtras() {
            return super.getDefaultViewModelCreationExtras();
        }
        private void saveSetting(String key, String value) {
            SettingsDatabaseManager settingsDatabaseManager = new SettingsDatabaseManager(getActivity());
            try {
                settingsDatabaseManager.open();
                settingsDatabaseManager.saveSetting(key, value);
                settingsDatabaseManager.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

    }

}
