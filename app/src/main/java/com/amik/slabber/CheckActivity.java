package com.amik.slabber;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class CheckActivity extends AppCompatActivity {

    private TextView StartText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HideActionBar();
        Init();
        ButtonOnClick();
    }

    private void Init(){
        // init textview and buttons
        StartText = findViewById(R.id.SaveTextView);
        InitAnimation();
    }

    private void ButtonOnClick(){
        StartText.setOnClickListener(v -> {
            Intent intent = new Intent(CheckActivity.this, WebActivity.class);
            startActivity(intent);
        });
    }

    private void InitAnimation(){
        StartText.setAnimation(AnimationUtils.loadAnimation(this, R.anim.maintext_animation));
    }

    private void HideActionBar(){
        Objects.requireNonNull(getSupportActionBar()).hide();
    }
}