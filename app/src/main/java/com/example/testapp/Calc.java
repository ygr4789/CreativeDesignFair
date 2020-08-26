package com.example.testapp;

import android.util.Log;

public class Calc {
    private static final float GRAVITATION = 9.80665f;

    public static float ax, ay, az;
    public static float gx, gy, gz;
    public static float mx, my, mz;

    public static float c11,c12,c13,c21,c22,c23,c31,c32,c33;
    public static float delX, delY, delZ;
    public static float vx, vy, vz;

    public static float q0,q1,q2,q3;
    private static float beta = 0.1f;
    private static float interval = 0.1f;
    public static float sampleFreq = 1/interval;


    public static void MadgwickAHRSupdate() {
        Log.d("Calc", "Madgwick filter");

        float recipNorm;
        float s0, s1, s2, s3;
        float qDot1, qDot2, qDot3, qDot4;
        float hx, hy;
        float _2q0mx, _2q0my, _2q0mz, _2q1mx, _2bx, _2bz, _4bx, _4bz, _2q0, _2q1, _2q2, _2q3, _2q0q2, _2q2q3, q0q0, q0q1, q0q2, q0q3, q1q1, q1q2, q1q3, q2q2, q2q3, q3q3;

        // Use IMU algorithm if magnetometer measurement invalid (avoids NaN in magnetometer normalisation)
        if((mx == 0.0f) && (my == 0.0f) && (mz == 0.0f)) {
            MadgwickAHRSupdateIMU();
            return;
        }

        // Rate of change of quaternion from gyroscope
        qDot1 = 0.5f * (-q1 * gx - q2 * gy - q3 * gz);
        qDot2 = 0.5f * (q0 * gx + q2 * gz - q3 * gy);
        qDot3 = 0.5f * (q0 * gy - q1 * gz + q3 * gx);
        qDot4 = 0.5f * (q0 * gz + q1 * gy - q2 * gx);

        // Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
        if(!((ax == 0.0f) && (ay == 0.0f) && (az == 0.0f))) {

            // Normalise accelerometer measurement
            recipNorm = invSqrt(ax * ax + ay * ay + az * az);
            ax *= recipNorm;
            ay *= recipNorm;
            az *= recipNorm;

            // Normalise magnetometer measurement
            recipNorm = invSqrt(mx * mx + my * my + mz * mz);
            mx *= recipNorm;
            my *= recipNorm;
            mz *= recipNorm;

            // Auxiliary variables to avoid repeated arithmetic
            _2q0mx = 2.0f * q0 * mx;
            _2q0my = 2.0f * q0 * my;
            _2q0mz = 2.0f * q0 * mz;
            _2q1mx = 2.0f * q1 * mx;
            _2q0 = 2.0f * q0;
            _2q1 = 2.0f * q1;
            _2q2 = 2.0f * q2;
            _2q3 = 2.0f * q3;
            _2q0q2 = 2.0f * q0 * q2;
            _2q2q3 = 2.0f * q2 * q3;
            q0q0 = q0 * q0;
            q0q1 = q0 * q1;
            q0q2 = q0 * q2;
            q0q3 = q0 * q3;
            q1q1 = q1 * q1;
            q1q2 = q1 * q2;
            q1q3 = q1 * q3;
            q2q2 = q2 * q2;
            q2q3 = q2 * q3;
            q3q3 = q3 * q3;

            // Reference direction of Earth's magnetic field
            hx = mx * q0q0 - _2q0my * q3 + _2q0mz * q2 + mx * q1q1 + _2q1 * my * q2 + _2q1 * mz * q3 - mx * q2q2 - mx * q3q3;
            hy = _2q0mx * q3 + my * q0q0 - _2q0mz * q1 + _2q1mx * q2 - my * q1q1 + my * q2q2 + _2q2 * mz * q3 - my * q3q3;
            _2bx = (float) Math.sqrt(hx * hx + hy * hy);
            _2bz = -_2q0mx * q2 + _2q0my * q1 + mz * q0q0 + _2q1mx * q3 - mz * q1q1 + _2q2 * my * q3 - mz * q2q2 + mz * q3q3;
            _4bx = 2.0f * _2bx;
            _4bz = 2.0f * _2bz;

            // Gradient decent algorithm corrective step
            s0 = -_2q2 * (2.0f * q1q3 - _2q0q2 - ax) + _2q1 * (2.0f * q0q1 + _2q2q3 - ay) - _2bz * q2 * (_2bx * (0.5f - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + (-_2bx * q3 + _2bz * q1) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + _2bx * q2 * (_2bx * (q0q2 + q1q3) + _2bz * (0.5f - q1q1 - q2q2) - mz);
            s1 = _2q3 * (2.0f * q1q3 - _2q0q2 - ax) + _2q0 * (2.0f * q0q1 + _2q2q3 - ay) - 4.0f * q1 * (1 - 2.0f * q1q1 - 2.0f * q2q2 - az) + _2bz * q3 * (_2bx * (0.5f - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + (_2bx * q2 + _2bz * q0) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + (_2bx * q3 - _4bz * q1) * (_2bx * (q0q2 + q1q3) + _2bz * (0.5f - q1q1 - q2q2) - mz);
            s2 = -_2q0 * (2.0f * q1q3 - _2q0q2 - ax) + _2q3 * (2.0f * q0q1 + _2q2q3 - ay) - 4.0f * q2 * (1 - 2.0f * q1q1 - 2.0f * q2q2 - az) + (-_4bx * q2 - _2bz * q0) * (_2bx * (0.5f - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + (_2bx * q1 + _2bz * q3) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + (_2bx * q0 - _4bz * q2) * (_2bx * (q0q2 + q1q3) + _2bz * (0.5f - q1q1 - q2q2) - mz);
            s3 = _2q1 * (2.0f * q1q3 - _2q0q2 - ax) + _2q2 * (2.0f * q0q1 + _2q2q3 - ay) + (-_4bx * q3 + _2bz * q1) * (_2bx * (0.5f - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + (-_2bx * q0 + _2bz * q2) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + _2bx * q1 * (_2bx * (q0q2 + q1q3) + _2bz * (0.5f - q1q1 - q2q2) - mz);
            recipNorm = invSqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3); // normalise step magnitude
            s0 *= recipNorm;
            s1 *= recipNorm;
            s2 *= recipNorm;
            s3 *= recipNorm;

            // Apply feedback step
            qDot1 -= beta * s0;
            qDot2 -= beta * s1;
            qDot3 -= beta * s2;
            qDot4 -= beta * s3;
        }

        // Integrate rate of change of quaternion to yield quaternion
        q0 += qDot1 * (1.0f / sampleFreq);
        q1 += qDot2 * (1.0f / sampleFreq);
        q2 += qDot3 * (1.0f / sampleFreq);
        q3 += qDot4 * (1.0f / sampleFreq);

        // Normalise quaternion
        recipNorm = invSqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 *= recipNorm;
        q1 *= recipNorm;
        q2 *= recipNorm;
        q3 *= recipNorm;
    }

//---------------------------------------------------------------------------------------------------
// IMU algorithm update

    private static void MadgwickAHRSupdateIMU() {

        float recipNorm;
        float s0, s1, s2, s3;
        float qDot1, qDot2, qDot3, qDot4;
        float _2q0, _2q1, _2q2, _2q3, _4q0, _4q1, _4q2 ,_8q1, _8q2, q0q0, q1q1, q2q2, q3q3;

        // Rate of change of quaternion from gyroscope
        qDot1 = 0.5f * (-q1 * gx - q2 * gy - q3 * gz);
        qDot2 = 0.5f * (q0 * gx + q2 * gz - q3 * gy);
        qDot3 = 0.5f * (q0 * gy - q1 * gz + q3 * gx);
        qDot4 = 0.5f * (q0 * gz + q1 * gy - q2 * gx);

        // Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
        if(!((ax == 0.0f) && (ay == 0.0f) && (az == 0.0f))) {

            // Normalise accelerometer measurement
            recipNorm = invSqrt(ax * ax + ay * ay + az * az);
            ax *= recipNorm;
            ay *= recipNorm;
            az *= recipNorm;

            // Auxiliary variables to avoid repeated arithmetic
            _2q0 = 2.0f * q0;
            _2q1 = 2.0f * q1;
            _2q2 = 2.0f * q2;
            _2q3 = 2.0f * q3;
            _4q0 = 4.0f * q0;
            _4q1 = 4.0f * q1;
            _4q2 = 4.0f * q2;
            _8q1 = 8.0f * q1;
            _8q2 = 8.0f * q2;
            q0q0 = q0 * q0;
            q1q1 = q1 * q1;
            q2q2 = q2 * q2;
            q3q3 = q3 * q3;

            // Gradient decent algorithm corrective step
            s0 = _4q0 * q2q2 + _2q2 * ax + _4q0 * q1q1 - _2q1 * ay;
            s1 = _4q1 * q3q3 - _2q3 * ax + 4.0f * q0q0 * q1 - _2q0 * ay - _4q1 + _8q1 * q1q1 + _8q1 * q2q2 + _4q1 * az;
            s2 = 4.0f * q0q0 * q2 + _2q0 * ax + _4q2 * q3q3 - _2q3 * ay - _4q2 + _8q2 * q1q1 + _8q2 * q2q2 + _4q2 * az;
            s3 = 4.0f * q1q1 * q3 - _2q1 * ax + 4.0f * q2q2 * q3 - _2q2 * ay;
            recipNorm = invSqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3); // normalise step magnitude
            s0 *= recipNorm;
            s1 *= recipNorm;
            s2 *= recipNorm;
            s3 *= recipNorm;

            // Apply feedback step
            qDot1 -= beta * s0;
            qDot2 -= beta * s1;
            qDot3 -= beta * s2;
            qDot4 -= beta * s3;
        }

        // Integrate rate of change of quaternion to yield quaternion
        q0 += qDot1 * (1.0f / sampleFreq);
        q1 += qDot2 * (1.0f / sampleFreq);
        q2 += qDot3 * (1.0f / sampleFreq);
        q3 += qDot4 * (1.0f / sampleFreq);

        // Normalise quaternion
        recipNorm = invSqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 *= recipNorm;
        q1 *= recipNorm;
        q2 *= recipNorm;
        q3 *= recipNorm;
    }

    //---------------------------------------------------------------------------------------------------
// Fast inverse square-root
    static float invSqrt(float x) {
        float xhalf = 0.5f * x;
        int i = Float.floatToIntBits( x );
        i = 0x5f3759df - (i >> 1);
        x = Float.intBitsToFloat( i );
        x = x * (1.5f - (xhalf * x * x));
        return x;
    }

    public static void quaternionToMatrix(){
        c11 = 2.0f*q0*q0-1.0f+2.0f*q1*q1;
        c12 = 2.0f*q1*q2+2.0f*q0*q3;
        c13 = 2.0f*q1*q3-2.0f*q0*q2;
        c21 = 2.0f*q1*q2-2.0f*q0*q3;
        c22 = 2.0f*q0*q0-1.0f+2.0f*q2*q2;
        c23 = 2.0f*q2*q3+2.0f*q0*q1;
        c31 = 2.0f*q1*q3+2.0f*q0*q1;
        c32 = 2.0f*q2*q3-2.0f*q0*q1;
        c33 = 2.0f*q0*q0-1.0f+2.0f*q3*q3;
    }

    public static void vupdate(){
        vx += interval*(c11*ax+c12*ay+c13*az)*GRAVITATION;
        vy += interval*(c21*ax+c22*ay+c23*az)*GRAVITATION;
        vz += interval*(c31*ax+c32*ay+c33*az+1.0f)*GRAVITATION;
    }

    public static void pupdate(){
        delX += interval*vx;
        delY += interval*vy;
        delZ += interval*vz;
    }

    public static void getInitialQuaternion(){
        float x1 ,x2, x3, y1, y2, y3, z1, z2, z3;
        float m00, m01, m02, m10, m11, m12, m20, m21, m22;
        float magx, magy, magz;

        z1 = -ax; z2 = -ay; z3 = -az;
        y1 = my*az-mz*ay; y2 = -mx*az+mz*ax; y3 = mx*ay-my*ax;
        x1 = y2*z3-y3*z2; x2 = -y1*z3+y3*z1; x3 = y1*z2-y2*z1;

        magx = (float) Math.sqrt(x1*x1+x2*x2+x3*x3);
        magy = (float) Math.sqrt(y1*y1+y2*y2+y3*y3);
        magz = (float) Math.sqrt(z1*z1+z2*z2+z3*z3);

        m00 = x1/magx; m01 = x2/magx; m02 = x3/magx;
        m10 = y1/magy; m11 = y2/magy; m12 = y3/magy;
        m20 = z1/magz; m21 = z2/magz; m22 = z3/magz;

        float tr = m00 + m11 + m22;

        if (tr > 0.0f) {
            float S = (float) (Math.sqrt(tr+1.0f) * 2.0f); // S=4*qw
            q0 = 0.25f * S;
            q1 = (m21 - m12) / S;
            q2 = (m02 - m20) / S;
            q3 = (m10 - m01) / S;
        } else if ((m00 > m11)&&(m00 > m22)) {
            float S = (float) (Math.sqrt(1.0f + m00 - m11 - m22) * 2.0f); // S=4*qx
            q0 = (m21 - m12) / S;
            q1 = 0.25f * S;
            q2 = (m01 + m10) / S;
            q3 = (m02 + m20) / S;
        } else if (m11 > m22) {
            float S = (float) (Math.sqrt(1.0f + m11 - m00 - m22) * 2.0f); // S=4*qy
            q0 = (m02 - m20) / S;
            q1 = (m01 + m10) / S;
            q2 = 0.25f * S;
            q3 = (m12 + m21) / S;
        } else {
            float S = (float) (Math.sqrt(1.0f + m22 - m00 - m11) * 2.0f); // S=4*qz
            q0 = (m10 - m01) / S;
            q1 = (m02 + m20) / S;
            q2 = (m12 + m21) / S;
            q3 = 0.25f * S;
        }
    }

}