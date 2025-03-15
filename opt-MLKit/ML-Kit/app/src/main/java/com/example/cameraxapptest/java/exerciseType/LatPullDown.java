package com.example.cameraxapptest.java.exerciseType;

import com.example.cameraxapptest.java.TargetPose;
import com.example.cameraxapptest.java.TargetShape;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.Arrays;

public class LatPullDown implements MakePose{

    private static boolean isLatPullDown = false;
    public TargetPose targetPose() {
        TargetPose latPullDown = new TargetPose(Arrays.asList(
            new TargetShape(PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_SHOULDER, 90, 150),
                new TargetShape(PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_SHOULDER, 90, 150),
                new TargetShape(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP, 50, 130),
                new TargetShape(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP, 50, 130)
        ));
        return latPullDown;
    }

    @Override
    public boolean isCount(double startAngle, double endAngle, double angle, Pose pose, int index) {
        if (angle < startAngle && !isLatPullDown) {
            isLatPullDown = true;
        } else if (angle > endAngle && isLatPullDown) {
            isLatPullDown = false;
            return true;
        }
        return false;
    }

}
