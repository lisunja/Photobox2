package com.example.photobox.view;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.viewmodel.CreationExtras;
import androidx.preference.PreferenceFragmentCompat;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.photobox.R;
import com.example.photobox.utils.SecureStorage;

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
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
            if (key.equals("password")){
                SecureStorage secureStorage = new SecureStorage(getContext());
                String value = sharedPreferences.getString(key, "");
                secureStorage.saveValue(value, "password");
            }
            if (key.equals("ip")){
                SecureStorage secureStorage = new SecureStorage(getContext());
                String value = sharedPreferences.getString(key, "");
                secureStorage.saveValue(value, "ip");
            }
            if (key.equals("username")){
                SecureStorage secureStorage = new SecureStorage(getContext());
                String value = sharedPreferences.getString(key, "");
                secureStorage.saveValue(value, "username");
            }
            if (key.equals("share")){
                SecureStorage secureStorage = new SecureStorage(getContext());
                String value = sharedPreferences.getString(key, "");
                secureStorage.saveValue(value, "share");
            }
            if (key.equals("remote_path")){
                SecureStorage secureStorage = new SecureStorage(getContext());
                String value = sharedPreferences.getString(key, "");
                secureStorage.saveValue(value, "remote_path");
            }
            if (key.equals("year")){
                SecureStorage secureStorage = new SecureStorage(getContext());
                String value = sharedPreferences.getString(key, "");
                if (value.matches("\\d{2}")) {
                    secureStorage.saveValue(value, "year");
                } else {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Invalid Input")
                            .setMessage("Please enter a valid two-digit year (e.g., 24 for 2024).")
                            .setPositiveButton("OK", null)
                            .show();
                }
            }
//            else {
//                String value = sharedPreferences.getString(key, "");
//                saveSetting(key, value);
//            }
        }

        @NonNull
        @Override
        public CreationExtras getDefaultViewModelCreationExtras() {
            return super.getDefaultViewModelCreationExtras();
        }
//        private void saveSetting(String key, String value) {
//            SettingsDatabaseManager settingsDatabaseManager = new SettingsDatabaseManager(getActivity());
//            try {
//                settingsDatabaseManager.open();
//                settingsDatabaseManager.saveSetting(key, value);
//                settingsDatabaseManager.close();
//            }
//            catch (Exception e){
//                e.printStackTrace();
//            }
//        }

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
