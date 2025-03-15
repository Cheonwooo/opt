package com.example.cameraxapptest

import android.util.Log
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

    data class TargetShape(
        val firstLandmarkType: Int,
        val middleLandmarkType: Int,
        val lastLandmarkType: Int,
        val angle: Double
    )
    data class TargetPose(
        val targets: List<TargetShape>
    )

    class poseMatcher {


        fun match(pose: Pose, targetPose: TargetPose): Boolean {
            return extractAndMatch(pose, targetPose)
        }

        private fun extractAndMatch(pose: Pose, targetPose: TargetPose) : Boolean {

            targetPose.targets.forEach { target ->
                val (firstLandmark, middleLandmark, lastLandmark) = extractLandmark(pose, target)


                //Check landmark is null
                if (landmarkNotFound(firstLandmark, middleLandmark, lastLandmark)) {
                    return false
                }

                val angle = calculateAngle(firstLandmark!!, middleLandmark!!, lastLandmark!!)
                val targetAngle = target.angle
                if (abs(angle - targetAngle) > offset) {
                    return false
                }
                println("firstLandmark : " + firstLandmark + " middleLandmark " + middleLandmark + " lastLandmark " + lastLandmark + " angle : " + angle);
            }
            return true
        }

        private fun extractLandmark(
            pose: Pose,
            target: TargetShape
        ): Triple<PoseLandmark?, PoseLandmark?, PoseLandmark?> {
            return Triple(
                extractLandmarkFromType(pose, target.firstLandmarkType),
                extractLandmarkFromType(pose, target.middleLandmarkType),
                extractLandmarkFromType(pose, target.lastLandmarkType)
            )
        }
        private fun extractLandmarkFromType(pose: Pose, landmarkType: Int): PoseLandmark? {
            return pose.getPoseLandmark(landmarkType)
        }
        private fun landmarkNotFound(
            firstLandmark: PoseLandmark?,
            middleLandmark: PoseLandmark?,
            lastLandmark: PoseLandmark?
        ): Boolean {
            return firstLandmark == null || middleLandmark == null || lastLandmark == null
        }
        private fun calculateAngle(
            firstLandmark: PoseLandmark,
            middleLandmark: PoseLandmark,
            lastLandmark: PoseLandmark
        ): Double {
            val angle = Math.toDegrees(
                (atan2(
                    lastLandmark.position3D.y - middleLandmark.position3D.y,
                    lastLandmark.position3D.x - middleLandmark.position3D.x
                ) - atan2(
                    firstLandmark.position3D.y - middleLandmark.position3D.y,
                    firstLandmark.position3D.x - middleLandmark.position3D.x
                )).toDouble()
            )
            var absoluteAngle = abs(angle)
            if (absoluteAngle > 180) {
                absoluteAngle = 360 - absoluteAngle
            }
            return absoluteAngle
        }
        private fun anglesMatch(angle: Double, targetAngle: Double): Boolean {
            return angle < targetAngle + offset && angle > targetAngle - offset
        }
        companion object {
            private const val offset = 15.0
        }
    }


