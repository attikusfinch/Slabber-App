package com.amik.slabber;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SavedActivity extends AppCompatActivity {

    public LoadFilesFromDisk fileProcessor;
    public ListView filesList;

    public ImageView goBack;

    private LinearLayout progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);
        Objects.requireNonNull(getSupportActionBar()).hide();
        Init();
        setUpViews();
    }

    private void Init(){
        progressBar = findViewById(R.id.progressLayout);
        filesList = findViewById(R.id.filesList);
        goBack = findViewById(R.id.goBack);
    }

    @Override
    protected void onStart(){
        super.onStart();
        fileProcessor = new LoadFilesFromDisk();
        fileProcessor.execute();
    }

    @Override
    protected void onStop(){
        super.onStop();
        fileProcessor.cancel(true);
    }

    private void setUpViews() {
        goBack.setOnClickListener(v -> onBackPressed());
    }

    @SuppressLint("StaticFieldLeak")
    public class LoadFilesFromDisk extends AsyncTask<Void, Integer, List<File>>{

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<File> result){
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);
            setUpFileList(result);
        }

        @Override
        protected List<File> doInBackground(Void... voids) {
            File savedPagesFolder = new File(getFilesDir().getAbsolutePath());
            File[] filelist = savedPagesFolder.listFiles();
            assert filelist != null;
            return Arrays.asList(filelist);
        }
    }

    private void setUpFileList(List<File> result){
        List<File> fileList = new ArrayList<File>();

        List<File> pathList = new ArrayList<File>();

        for(File item : result) {
            if(!item.getPath().endsWith(".mhtml")){
                continue;
            }
            fileList.add(new File(item.getName().substring(1, item.getName().lastIndexOf(".mhtml"))));
            pathList.add(item);
        }

//        for(int i = 0; i < result.size(); i++) {
//            File item = result.get(i);
//            if(!item.getPath().endsWith(".mhtml")){
//                result.remove(i);
//                i--;
//                continue;
//            }
//            fileList.add(new File(item.getName().substring(1, item.getName().lastIndexOf(".mhtml"))));
//        }

        filesList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList));
        filesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SavedActivity.this, WebActivity.class);
                WebActivity webActivity = new WebActivity();
                intent.putExtra(webActivity.FILE_DATA, pathList.get(position).getPath());
                //startActivityForResult(intent,webActivity.FILE_OPEN_REQUEST);
                setResult(webActivity.FILE_OPEN_REQUEST, intent);
                finish();
            }
        });
    }
}
