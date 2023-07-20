package com.example.finestapp.scanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finestapp.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class ScannerQr extends AppCompatActivity {
    private Button backbtn;

    private boolean isProcessingBarcode = false;
    private SurfaceView surfaceView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private ToneGenerator toneGen1;
    private TextView barcodeText;
    private String barcodeData;
    private ProgressBar progressBar;
    private CountDownTimer timer;
    private static final int TIMER_DURATION = 5000; // 5 seconds


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_qr);

        backbtn = findViewById(R.id.backbtn);
        barcodeText = findViewById(R.id.barcodeText);
        surfaceView = findViewById(R.id.surface_view);
        progressBar = findViewById(R.id.progressBar);
        timer = createTimer(TIMER_DURATION);

        initialiseDetectorsAndSources();

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }



    private void initialiseDetectorsAndSources() {

        //Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1080, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScannerQr.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(ScannerQr.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0 && !isProcessingBarcode) {
                    isProcessingBarcode = true;

                    Barcode barcode = barcodes.valueAt(0);
                    if (barcode.email != null) {
                        barcodeData = barcode.email.address;
                    } else {
                        barcodeData = barcode.displayValue;
                        barcodeText.setText(barcodeData);
                        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);

                        // Start the timer and show the progress bar
                        timer.start();
                    }
                }
            }
        });
    }
//timer
    private CountDownTimer createTimer(long duration) {
        return new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the progress bar based on the remaining time
                int progress = (int) (millisUntilFinished / 1000);
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                // Timer finished, reset the progress bar and allow scanning again
                progressBar.setProgress(0);
                isProcessingBarcode = false;

                // Reset the timer
                timer.cancel();
                timer = createTimer(TIMER_DURATION);
            }
        };
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}