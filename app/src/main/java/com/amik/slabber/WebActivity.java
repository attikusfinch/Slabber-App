package com.amik.slabber;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private WebView webView;

    private FloatingActionButton mFloatingButton;
    private FloatingActionButton ReloadButton, RandomButton, TimeButton, VoiceButton;
    private CardView TimePanel;

    private TextToSpeech tts;

    private TextView TimeIndicator;

    private String TAG = "WebActivity";

    boolean isAllFabVisible;

    boolean isTTSAvailable;

    private static final int TIME_INTERVAL = 2000; // milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    public static final int REQUEST_SELECT_FILE = 100;
    public ValueCallback<Uri[]> uploadMessage;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_FILE) {
            if (uploadMessage == null) return;
            uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            uploadMessage = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        tts = new TextToSpeech(this, this);

        final Intent intent = getIntent();

        HideActionBar();
        WebViewConfigure();
        InitFabButton();
        InitFirebase();
        if(intent.getData() != null){
            OpenLink(intent.getDataString());
        }
    }

    private void InitFirebase(){
        FirebaseMessaging.getInstance().subscribeToTopic("Slabber")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(WebActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void OpenLink(String link) {
        webView.loadUrl(Uri.parse(link).toString());
    }

    private void InitFabButton(){
        mFloatingButton = findViewById(R.id.mainbutton);

        // find mini buttons
        ReloadButton = findViewById(R.id.reload);
        TimeButton = findViewById(R.id.time);
        VoiceButton = findViewById(R.id.voice);
        RandomButton = findViewById(R.id.random);
        TimePanel = findViewById(R.id.timeInfo);

        TimeIndicator = findViewById(R.id.TimeInfoIndicator);

        // make it invisible
        TimeButton.setVisibility(View.GONE);
        VoiceButton.setVisibility(View.GONE);
        ReloadButton.setVisibility(View.GONE);
        RandomButton.setVisibility(View.GONE);
        TimePanel.setVisibility(View.GONE);

        ButtonOnClick();
    }

    private void ButtonOnClick(){
        mFloatingButton.setOnClickListener(view -> {
            if(!isAllFabVisible){
                isAllFabVisible = true;
                if(webView.getUrl().contains("/posts/") && !webView.getUrl().contains("edit")){
                    TimeButton.show();
                    TimePanel.setVisibility(View.VISIBLE);
                    TextToTime();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isTTSAvailable) {
                        VoiceButton.show();
                    }
                }
                ReloadButton.show();
                RandomButton.show();
            }else{
                isAllFabVisible = false;
                TimeButton.hide();
                VoiceButton.hide();
                ReloadButton.hide();
                RandomButton.hide();
                TimePanel.setVisibility(View.GONE);
            }
        });
        RandomButton.setOnClickListener(view -> {
            Random rand = new Random();
            int random = rand.nextInt(200);
            webView.loadUrl("https://slabber.io/posts/" + random);
        });
        ReloadButton.setOnClickListener(view -> webView.reload());
        TimeButton.setOnClickListener(view -> TextToTime());
        VoiceButton.setOnClickListener(view -> GetTextFromPage());
    }

    private void TextToTime(){
        webView.evaluateJavascript(
        "(function() { var text = document.getElementsByClassName(\"editor-js-content\");\n" +
                "    var text_block = document.getElementsByClassName(\"editor-js-block\");\n" +
                "    var text_array = [];\n" +
                "    for (var i = 0; i < text.length; i++) {\n" +
                "        text_array.push(text[i].innerText);\n" +
                "    }\n" +
                "    for (var i = 0; i < text_block.length; i++) {\n" +
                "        text_array.push(text_block[i].innerText);\n" +
                "    }\n" +
                "    var text_string = text_array.join(\" \");\n" +
                "    var word_count = text_string.split(\" \").length;\n" +
                "    var words_per_minute = 150;\n" +
                "\n" +
                "    var minutes = Math.floor(word_count / words_per_minute);\n" +
                "    var seconds = Math.floor(word_count % words_per_minute / (words_per_minute / 60));\n" +
                "\n" +
                "    var str_minutes = (minutes == 1) ? \"m\" : \"m\";\n" +
                "    var str_seconds = (seconds == 1) ? \"s\" : \"s\";\n" +
                "\n" +
                "    if (minutes == 0) {\n" +
                "        return seconds + \" \" + str_seconds;\n" +
                "    } else {\n" +
                "        return minutes + \" \" + str_minutes;\n" +
                "    } })();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                TimeIndicator.setText(strip(s, "\""));
            }
        });
    }

    private String strip(String text, String tag){
        // js return string with "" around it, so we need to strip it
        return text.replaceAll(tag, "");
    }

    private void GetTextFromPage(){
        webView.evaluateJavascript(
            "(function() { var text = document.getElementsByClassName(\"bPage__text\");\n" +
                    "return text[0].innerText })();", new ValueCallback<String>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onReceiveValue(String s) {
                    speakOut(s);
                }
            });
    }

    @Override
    public void onBackPressed() {
        // if back button is pressed twice, exit the app
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
            {
                super.onBackPressed();
                return;
            }
            else { Toast.makeText(getBaseContext(), "Tap back button in order to exit", Toast.LENGTH_SHORT).show(); }

            mBackPressed = System.currentTimeMillis();
        }
    }

    private void HideActionBar(){
        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void WebViewConfigure(){
        webView = (WebView) findViewById (R.id.webView);

        webView.setWebViewClient(new WebViewClientOptions());
        webView.setWebChromeClient(new WebActivity.ChromeClient());

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUserAgentString(webSettings.getUserAgentString());

        WebView.setWebContentsDebuggingEnabled(true);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webView.loadUrl("https://slabber.io/");

        // Cookie manager for the webview
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
    }

    @Override
    public void onInit(int status) {
        Locale locale = new Locale("RU");
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported");
                isTTSAvailable = false;
            } else {
                //tts.speak("Лох", TextToSpeech.QUEUE_ADD, null);
                Log.e("TTS", "Language is supported");
                tts.setPitch(1.0f);
                tts.setSpeechRate(0.8f);
                isTTSAvailable = true;
            }
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void speakOut(String text) {
        if(tts.isSpeaking()){
            tts.stop();
            Toast.makeText(this, "Voice stop", Toast.LENGTH_SHORT).show();
            return;
        }
        Locale locale = new Locale("RU");
        text = text.replace("\\n", "");
        text = text.replaceAll("http.*?\\s", " ");
        int result = tts.setLanguage(locale);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("TTS", "Language is not supported");
        } else {
            Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)", Pattern.MULTILINE | Pattern.COMMENTS);
            Matcher reMatcher = re.matcher(text);

            int position=0 ;
            int sizeOfChar= text.length();
            String testString= text.substring(position,sizeOfChar);
            while(reMatcher.find()) {
                String temp;
                try {
                    temp = testString.substring(text.lastIndexOf(reMatcher.group()), text.indexOf(reMatcher.group())+reMatcher.group().length());
                    tts.speak(temp, TextToSpeech.QUEUE_ADD, null,"speak");
                } catch (Exception e) {
                    temp = testString.substring(0, testString.length());
                    tts.speak(temp, TextToSpeech.QUEUE_ADD, null);
                    break;
                }
            }
        }
    }

    private class ChromeClient extends WebChromeClient {
        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        protected FrameLayout mFullscreenContainer;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            // make sure there is no existing message
            if (WebActivity.this.uploadMessage != null) {
                WebActivity.this.uploadMessage.onReceiveValue(null);
                WebActivity.this.uploadMessage = null;
            }

            WebActivity.this.uploadMessage = filePathCallback;

            Intent intent = fileChooserParams.createIntent();
            try {
                WebActivity.this.startActivityForResult(intent, WebActivity.this.REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e) {
                WebActivity.this.uploadMessage = null;
                Toast.makeText(WebActivity.this, "Cannot open file chooser", Toast.LENGTH_LONG).show();
                return false;
            }

            return true;
        }

        ChromeClient() { }

        public Bitmap getDefaultVideoPoster()
        {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView()
        {
            ((FrameLayout) getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback)
        {
            if (this.mCustomView != null)
            {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }
}
