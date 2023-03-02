package com.amik.slabber;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.amik.slabber.Security.EncryptedPreferenceDataStore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private String TAG = "SettingsActivity";

    private TextView StartText;

    private EditText PasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Objects.requireNonNull(getSupportActionBar()).hide();
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        HideActionBar();
        Init();
        ButtonOnClick();
    }

    private void UpdateSettings(){
        EncryptedPreferenceDataStore prefs = new EncryptedPreferenceDataStore(this);

        boolean isNotificationPost = prefs.getBoolean("allow_post", true);
        boolean isNotificationComment = prefs.getBoolean("allow_comments", true);

        if(isNotificationPost){
            SubscribeNotificationPost();
        } else {
            UnSubscribeNotificationPost();
        }

        if(isNotificationComment){
            SubscribeNotificationComment();
        } else {
            UnSubscribeNotificationComment();
        }
    }

    private void Init(){
        // init textview and buttons
        StartText = findViewById(R.id.SaveTextView);

        InitAnimation();
    }

    private void ButtonOnClick(){
        StartText.setOnClickListener(v -> {
            UpdateSettings();
            Intent intent = new Intent(this, WebActivity.class);
            startActivity(intent);
        });
    }

    private void SubscribeNotificationPost(){
        FirebaseMessaging.getInstance().subscribeToTopic("Slabber")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                        Log.d(TAG, msg);
                        //Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void SubscribeNotificationComment(){
        FirebaseMessaging.getInstance().subscribeToTopic("SlabberNotification")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed Notification";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                        Log.d(TAG, msg);
                        //Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void UnSubscribeNotificationPost(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic("Slabber")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "UnSubscribed";
                        if (!task.isSuccessful()) {
                            msg = "UnSubscribe failed";
                        }
                        Log.d(TAG, msg);
                        //Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void UnSubscribeNotificationComment(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic("SlabberNotification")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "UnSubscribed Notification";
                        if (!task.isSuccessful()) {
                            msg = "UnSubscribe failed";
                        }
                        Log.d(TAG, msg);
                        //Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void InitAnimation(){
        StartText.setAnimation(AnimationUtils.loadAnimation(this, R.anim.maintext_animation));
    }

    private void HideActionBar(){
        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            PreferenceManager preferenceManager = getPreferenceManager();
            preferenceManager.setPreferenceDataStore(new EncryptedPreferenceDataStore(getContext()));

            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}