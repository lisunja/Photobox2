package com.example.photobox.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SecureStorage {
    private static final String SHARED_PREFS_NAME = "secret_shared_prefs";
    private final Context context;

    public SecureStorage(Context context) {
        this.context = context;
    }

    public void saveValue(String value, String name){
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "secret_shared_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(name, value);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String getUsername() {
        return getStringFromSharedPreferences("username");
    }

    public String getIp() {
        return getStringFromSharedPreferences("ip");
    }

    public String getShare() {
        return getStringFromSharedPreferences("share");
    }

    public String getPassword() {
        return getStringFromSharedPreferences("password");
    }

    public String getRemotePath() {
        return getStringFromSharedPreferences("remote_path");
    }
    public String getYear() {
        return getStringFromSharedPreferences("year");
    }
    private String getStringFromSharedPreferences(String key) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    SHARED_PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            return sharedPreferences.getString(key, null);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
