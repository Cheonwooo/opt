package com.example.cameraxapptest.java;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.example.cameraxapptest.R;
import com.example.cameraxapptest.databinding.ActivityMainBinding;
import com.example.cameraxapptest.java.exerciseType.MakePose;
import com.example.cameraxapptest.java.exerciseType.OSign;
import com.example.cameraxapptest.java.exerciseType.Squat;
import com.example.cameraxapptest.java.exerciseType.LatPullDown;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;

import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity_Java extends AppCompatActivity {

    private static final String TAG = "CameraXApp";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private static final Map<String, MakePose> poseMap = Map.ofEntries(
            Map.entry("스쿼트", new Squat()),
            Map.entry("동그라미", new OSign()),
            Map.entry("랫풀다운", new LatPullDown())
    );
    private static boolean isPoseDetectionEnabled = false;
    private static int count;
    private PoseDetectorOptions options = new PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build();
    private PoseDetector poseDetector = PoseDetection.getClient(options);
    private ActivityMainBinding viewBinding;
    private ExecutorService cameraExecutor;
    private PoseMatcher_Java poseMatcher = new PoseMatcher_Java();
    private MakePose currentPose;
    private TextView tvCount;

    public void setPose(MakePose pose) {
        this.currentPose = pose;
    }

    public void onPoseDetected(Pose pose) {
        if (currentPose != null) {
            boolean isMatched = poseMatcher.match(pose, currentPose);
            if (isMatched) {
                tvCount.setText(count + "회!");
            }
        }
    }

    private final ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                if (permissions.values().stream().allMatch(Boolean::booleanValue)) {
                    startCamera();
                } else {
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        tvCount = findViewById(R.id.tvCount);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            permissionsLauncher.launch(REQUIRED_PERMISSIONS);
        }

        Spinner exerciseSpinner = findViewById(R.id.exerciseSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(poseMap.keySet()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseSpinner.setAdapter(adapter);

        final String[] selectedExercise = {adapter.getItem(0)};

        exerciseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                count = 0;
                tvCount = findViewById(R.id.tvCount);
                selectedExercise[0] = parent.getItemAtPosition(position).toString();
                Toast.makeText(MainActivity_Java.this, "선택한 운동: " + selectedExercise[0], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedExercise[0] = null;
            }
        });


        Handler handler = new Handler(Looper.getMainLooper());
        Runnable poseDetectionRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPoseDetectionEnabled) {
                    MakePose selectedPose = poseMap.get(selectedExercise[0]);
                    if (selectedPose != null) {
                        setPose(selectedPose);
                    }
                    handler.postDelayed(this, 100);
                }
            }
        };

        viewBinding.imageCaptureButton.setOnClickListener(v -> {
            isPoseDetectionEnabled = !isPoseDetectionEnabled;
            if (isPoseDetectionEnabled) {
                Toast.makeText(this, "실시간 포즈 감지 시작", Toast.LENGTH_SHORT).show();
                handler.post(poseDetectionRunnable);
            } else {
                Toast.makeText(this, "실시간 포즈 감지 중지", Toast.LENGTH_SHORT).show();
                handler.removeCallbacks(poseDetectionRunnable);
            }
        });

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private boolean allPermissionsGranted() {
        return Arrays.stream(REQUIRED_PERMISSIONS)
                .allMatch(permission -> ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewBinding.viewFinder.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, new CameraAnalyzer(poseDetector, this::onPoseDetected));

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalysis
                );

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
