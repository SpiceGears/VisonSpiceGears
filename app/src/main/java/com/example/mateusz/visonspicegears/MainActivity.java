package com.example.mateusz.visonspicegears;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.VideoView;

import java.io.IOException;


public class MainActivity extends AppCompatActivity  {

    final int REQUEST_CAMERA_PERMISSION = 1;
    VideoView videoView;
    VisionTrackerGLSurfaceView view;
    public static VisionServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        try {
            if ( server == null )
                server = new VisionServer(this, 9225);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0); // Just blow up if we can't start listening
        }

        System.loadLibrary("native-vision");
        tryStartCamera();
        videoView = findViewById(R.id.videoView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    public void tryStartCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);

        }

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            tryStartCamera();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    public void changeConnectionState(boolean connected) {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(connected?Color.GREEN:Color.RED));
    }


}