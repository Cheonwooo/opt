package com.example.cameraxapptest.java.exerciseType;

import com.example.cameraxapptest.java.TargetPose;
import com.example.cameraxapptest.java.TargetShape;
import com.google.mlkit.vision.pose.Pose;

public interface MakePose {
    TargetPose targetPose();
    boolean isCount(double startAngle, double endAngle, double angle, Pose pose, int index);
}
