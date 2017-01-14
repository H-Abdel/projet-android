package com.m2.dev.projetm2dciss;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;

public class Orientation implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensorBoussole;

    private float[] mOrientationVals = new float[3];

    private float[] mRotationMatrixMagnetic = new float[16];
    private float[] mRotationMatrixMagneticToTrue = new float[16];
    private float[] mRotationMatrix = new float[16];

    private OrientationListener orientationListener;

    public Orientation (SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        sensorBoussole = this.sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR) {
            return;
        }

        // Transforme la rotation vector en matrice de rotation
        SensorManager.getRotationMatrixFromVector(mRotationMatrixMagnetic, event.values );

        //Création de la matrice de passage de repère magnétique au repère classique
        Matrix.setRotateM(mRotationMatrixMagneticToTrue, 0, -1.83f, 0, 0, 1);

        //Change la matrice d'orientation du repère magnétique au repère classique
        Matrix.multiplyMM(mRotationMatrix, 0, mRotationMatrixMagnetic, 0, mRotationMatrixMagneticToTrue, 0);

        // Transforme la matrice de rotation en une succession de rotations autour de z , y e t x
        SensorManager.getOrientation(mRotationMatrix , mOrientationVals);

        orientationListener.onOrientationChanged(mOrientationVals[0]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void start () {
        sensorManager.registerListener(this, sensorBoussole, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop () {
        sensorManager.unregisterListener(this);
    }

    public void setOrientationListener (OrientationListener orientationListener) {
        this.orientationListener = orientationListener;
    }

    public interface OrientationListener {
        public void onOrientationChanged(float cap);
    }

}
