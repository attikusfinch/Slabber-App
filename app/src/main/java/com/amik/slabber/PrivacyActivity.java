package com.amik.slabber;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amik.slabber.Security.EncryptedPreferenceDataStore;

import java.util.Objects;

public class PrivacyActivity extends AppCompatActivity implements View.OnClickListener {

    private Button agree_button, disagree_button, privacy_policy_button;

    private CheckBox agree_checkbox;

    private ScrollView privacy_scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        HideActionBar();
        Init();
    }

    private void HideActionBar(){
        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    private boolean canScroll() {
        return privacy_scrollView.canScrollVertically(1);
    }

    private void Init(){
        agree_button = findViewById(R.id.agree_button);
        disagree_button = findViewById(R.id.disagree_button);
        privacy_policy_button = findViewById(R.id.privacy_policy_button);
        agree_checkbox = findViewById(R.id.agree_checkbox);
        privacy_scrollView = findViewById(R.id.scrollViewPrivacy);

        setOnClick();
    }

    private void setOnClick(){
        agree_button.setOnClickListener(this);
        disagree_button.setOnClickListener(this);
        privacy_policy_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == agree_button.getId()){
            if(agree_checkbox.isChecked() && !canScroll()){
                EncryptedPreferenceDataStore settings = new EncryptedPreferenceDataStore(this);
                settings.putBoolean("agree", true);

                Intent intent = new Intent(this, WebActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "You need scroll all UGC and check box", Toast.LENGTH_SHORT).show();
            }
        } else if(v.getId() == disagree_button.getId()){
            Toast.makeText(this, "App can't work without accept permissions", Toast.LENGTH_LONG).show();
            finish();
            System.exit(0);
        } else if (v.getId() == privacy_policy_button.getId()){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://pages.flycricket.io/slabber/privacy.html"));
            startActivity(browserIntent);
        }
    }
}
