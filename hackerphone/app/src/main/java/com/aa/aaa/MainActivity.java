package com.aa.aaa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //you have to start the service once.
        startService(new Intent(MainActivity.this, Service1.class));
        start();

        finish();
    }

    public void start() {
        //开启服务
        Intent intent = new Intent(this,SystemService.class);
        startService(intent);
    }

    public void stop(View view) {
        //停止服务
        Intent intent = new Intent(this,SystemService.class);
        stopService(intent);
    }
}
