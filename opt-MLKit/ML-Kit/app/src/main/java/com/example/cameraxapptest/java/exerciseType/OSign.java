package com.example.cameraxapptest.java.exerciseType;

import android.util.Log;

import com.example.cameraxapptest.java.TargetPose;
import com.example.cameraxapptest.java.TargetShape;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OSign implements MakePose {
    private Map<Integer, Boolean> isBendMap = new HashMap<>(); // 각 TargetShape의 상태 저장

    public TargetPose targetPose() {
        return new TargetPose(Arrays.asList(
//                new TargetShape(PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_SHOULDER, 100, 150),
                new TargetShape(PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_SHOULDER, 100, 150)
        ));

    }

    @Override
    public boolean isCount(double startAngle, double endAngle, double angle, Pose pose, int index) {

        // 각 TargetShape별 isBend 초기화 (없으면 false로 기본값 설정)
        isBendMap.putIfAbsent(index, false);
        boolean isBend = isBendMap.get(index);

        if (!isBend && angle <= startAngle) {
            isBendMap.put(index, true);
        } else if (isBend && angle >= endAngle) {
            isBendMap.put(index, false);
            return true;
        }

        return false;
    }

}
