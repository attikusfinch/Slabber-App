package com.amik.slabber;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class WebViewClientOptions extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String url) {
        if (url == null || url.startsWith("http://") || url.startsWith("https://")) return false;

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            webView.getContext().startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.i("ERROR", "shouldOverrideUrlLoading Exception:" + e);
            return true;
        }
    }

    private WebResourceResponse getTextWebResource(InputStream data) {
        return new WebResourceResponse("text/plain", "UTF-8", data);
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view,String url) {
        if(url.contains("google")||url.contains("facebook")){
            InputStream textStream = new ByteArrayInputStream("".getBytes());
            return getTextWebResource(textStream);
        }
        return super.shouldInterceptRequest(view, url);
    }
}
