package com.amik.slabber.WebView;

import android.app.AlertDialog;
import android.content.Context;

public class JavaScriptInterface {
    private Context ctx;

    JavaScriptInterface(Context ctx) {
        this.ctx = ctx;
    }

    public void showHTML(String html) {
        new AlertDialog.Builder(ctx).setTitle("HTML").setMessage(html)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .create()
                .show();
    }
}
