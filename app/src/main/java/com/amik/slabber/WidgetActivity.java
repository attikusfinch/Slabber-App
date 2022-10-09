package com.amik.slabber;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.amik.slabber.Widget.Parser;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.AppWidgetTarget;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetActivity extends AppWidgetProvider {

    private static final String SYNC_CLICKED    = "slabber_widget_update_action";
    private static final String WAITING_MESSAGE = "Wait for new post";

    private AppWidgetTarget appWidgetTarget;

    public static final int httpsDelayMs = 300;
    public String[] output = new String[]{};

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_activity);

        views.setTextViewText(R.id.WidgetTitle, WAITING_MESSAGE);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        String data[] = new String[]{};

        ParsePost parsePost = new ParsePost();

        try {
            data = parsePost.execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        //output = parsePost.getInfoString();

        views.setTextViewText(R.id.WidgetTitle, data[0]);
        views.setTextViewText(R.id.WidgetDescription, data[1]);
        //Picasso.get().load("https://slabber.io" + output[2]).into(remoteViews, R.layout.widget_activity, appWidgetIds);


        //при клике на виджет в систему отсылается вот такой интент, описание метода ниже
        views.setOnClickPendingIntent(R.id.WidgetImageView, getPendingSelfIntent(context, SYNC_CLICKED));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @SuppressLint({"RemoteViewLayout", "ResourceType"})
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        RemoteViews remoteViews;
        ComponentName watchWidget;

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_activity);
        watchWidget = new ComponentName(context, WidgetActivity.class);

        ParsePost parsePost = new ParsePost();

        try {
            output = parsePost.execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        //output = parsePost.getInfoString();

        remoteViews.setTextViewText(R.id.WidgetTitle, output[0]);
        remoteViews.setTextViewText(R.id.WidgetDescription, output[1]);

        appWidgetTarget = new AppWidgetTarget(context, R.id.WidgetImageView, remoteViews, appWidgetIds);

        SetImage(context,"https://slabber.io" + output[2]);
        //Picasso.get().load("https://slabber.io" + output[2]).into(remoteViews, R.layout.widget_activity, appWidgetIds);

        //при клике на виджет в систему отсылается вот такой интент, описание метода ниже
        remoteViews.setOnClickPendingIntent(R.id.WidgetImageView, getPendingSelfIntent(context, SYNC_CLICKED));
        appWidgetManager.updateAppWidget(watchWidget, remoteViews);

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private PendingIntent getPendingSelfIntent(Context context, String action) {
        // An explicit intent directed at the current class (the "self").
        Intent intent = new Intent(context, WidgetActivity.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 2000012, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        super.onReceive(context, intent);

        if (SYNC_CLICKED.equals(intent.getAction())) {

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            RemoteViews remoteViews;
            ComponentName watchWidget;

            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_activity);
            watchWidget = new ComponentName(context, WidgetActivity.class);

            //updating widget
            appWidgetManager.updateAppWidget(watchWidget, remoteViews);

            ParsePost parsePost = new ParsePost();

            try {
                output = parsePost.execute().get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            remoteViews.setTextViewText(R.id.WidgetTitle, output[0]);
            remoteViews.setTextViewText(R.id.WidgetDescription, output[1]);

            appWidgetTarget = new AppWidgetTarget(context, R.id.WidgetImageView, remoteViews, watchWidget);

            SetImage(context,"https://slabber.io" + output[2]);

            Toast.makeText(context.getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();

            //widget manager to update the widget
            appWidgetManager.updateAppWidget(watchWidget, remoteViews);
        }
    }

    private void SetImage(Context context, String url){
        Glide.with(context.getApplicationContext()) // safer!
                .asBitmap()
                .transform(new RoundedCorners(16))
                .error(R.drawable.slabber)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(12)))
                .into( appWidgetTarget );
    }

    @SuppressLint("StaticFieldLeak")
    class ParsePost extends AsyncTask<Void, Integer, String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected String[] doInBackground(Void... voids) {
            String[] data = new String[]{};
            try {
                Parser parser = new Parser();
                data = parser.getLastPost();

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String[] aVoid) {
            super.onPostExecute(aVoid);

            output = aVoid;
        }
    }
}