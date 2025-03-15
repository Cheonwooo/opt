package com.example.cameraxapptest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class MLKitActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ML Kit 실행 로직 (이 부분에 ML Kit 처리 코드 추가)
        Toast.makeText(this, "ML Kit 실행 중...", Toast.LENGTH_SHORT).show();

        // ML Kit 처리 완료 후 메인 앱으로 돌아가기
        finish();
    }
}