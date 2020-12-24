package com.example.ocr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Detector.Processor {

    private SurfaceView cameraView;
    private TextView mTextView;
    private CameraSource cameraSource;
    private Button btnStop;
    private boolean isStopTriggered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.surface_view);
        mTextView = findViewById(R.id.txtview);
        btnStop = findViewById(R.id.btnStop);

        btnStop.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                if (isStopTriggered) {
                    cameraSource.stop();
                    btnStop.setText("START THE SCAN");
                } else {
                    btnStop.setText("STOP THE SCAN");
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                isStopTriggered = !isStopTriggered;

            }
        });

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            Toast.makeText(MainActivity.this, "Detector Dependencies are missing.", Toast.LENGTH_LONG).show();
        } else {
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(this);
            textRecognizer.setProcessor(this);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
                return;
            }
            cameraSource.start(cameraView.getHolder());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        cameraSource.stop();
    }

    @Override
    public void release() {

    }

    @Override
    public void receiveDetections(@NonNull Detector.Detections detections) {
        SparseArray items = detections.getDetectedItems();
        final StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            TextBlock item = (TextBlock) items.valueAt(i);
            strBuilder.append(item.getValue());

            // The following Process is used to show how to use lines & elements as well

            /*for (int j = 0; j < items.size(); j++) {
                TextBlock textBlock = (TextBlock) items.valueAt(j);
                strBuilder.append(textBlock.getValue());
                strBuilder.append("/");
                for (Text line : textBlock.getComponents()) {
                    //extract scanned text lines here
                    Log.v("lines", line.getValue());
                    strBuilder.append(line.getValue());
                    strBuilder.append("/");
                    for (Text element : line.getComponents()) {
                        //extract scanned text words here
                        Log.v("element", element.getValue());
                        strBuilder.append(element.getValue());
                    }
                }
            }*/
        }
        Log.v("strBuilder.toString()", strBuilder.toString());

        mTextView.post(new Runnable() {
            @Override
            public void run() {
                mTextView.setText(strBuilder.toString());
            }
        });
    }
}