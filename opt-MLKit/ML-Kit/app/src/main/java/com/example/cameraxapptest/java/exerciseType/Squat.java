package com.example.cameraxapptest.java.exerciseType;

import android.util.Log;

import com.example.cameraxapptest.java.TargetPose;
import com.example.cameraxapptest.java.TargetShape;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Squat implements MakePose{

    private Map<Integer, Boolean> isSquattingMap = new HashMap<>();

    @Override
    public TargetPose targetPose() {
        TargetPose squat = new TargetPose(Arrays.asList(
//                new TargetShape(PoseLandmark.LEFT_ANKLE, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_HIP, 111, 160),
//                new TargetShape(PoseLandmark.RIGHT_ANKLE, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_HIP, 112, 160),
//                new TargetShape(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_SHOULDER, 113, 160),
                new TargetShape(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_SHOULDER, 114, 160)
        ));
        return squat;
    }

    @Override
    public boolean isCount(double startAngle, double endAngle, double angle, Pose pose, int index) {
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);

        if (leftHip == null || leftKnee == null || rightHip == null || rightKnee == null) {
            return false;
        }

        isSquattingMap.putIfAbsent(index, false);
        boolean isSquatting = isSquattingMap.get(index);

        Log.d("SUQAT","ISSQUATTING : " + ((isSquatting) ? "TRUE" : "FALSE") + "ANGLE : " + angle + "STARTANGLE : " + startAngle);
        if (!isSquatting && angle < startAngle
//                && (leftHip.getPosition().y > leftKnee.getPosition().y && rightHip.getPosition().y > rightKnee.getPosition().y)
        ) {
            isSquattingMap.put(index, true);
        } else if (isSquatting && angle > endAngle) {

            isSquattingMap.put(index, false);
            return true;
        }
        return false;
    }
}
