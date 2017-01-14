package com.m2.dev.projetm2dciss;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Date;

/**
 * Pedestrian Dead Reckoning
 */
public class PDR implements SensorEventListener {

    // Vecteur posistion [latitude, longitude] en degre
    private float[] mCurrentLocation = new float[2];

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    private float seuil;        // accélération au dessus delaquelle un pas est observé
    private boolean detectPas;  // Si vrai on observe le dépassement du seuil
    private Date detectTime;    // le moment où le dernier pas a été detécté

    private float capPas;       // L'angle du pas, vers le nord : capPas = 0
    private float taillePas;    // La taille du pas

    private int nbrePas = 0;    // nombre de pas effectué

    private Orientation orientation; // util pour le calcul de l'angle du pas

    private AccelerometerListener accelerometerListener;

    public PDR (SensorManager sensorManager) {

        mCurrentLocation[0] = 45.19283f;    // Latitude
        mCurrentLocation[1] =  5.77366f;    // Longitude

        this.sensorManager = sensorManager;
        accelerometerSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        seuil = 3f;
        detectPas = false;
        detectTime = new Date();
        taillePas = 0.7f;   // 70cm

        orientation = new Orientation(sensorManager);
        orientation.setOrientationListener(orientationListener);
    }

    // Calcul la nouvelle position en fonction de la taille du pas et de son angle
    public float[] computeNextStep(float taillePas, float bearing) {

        float[] newPosition = new float[2];
        float rayonGlobe = 6371000.0f;
        float angularDistance = taillePas/rayonGlobe;


        float latitude = (float) Math.asin( Math.sin(Math.toRadians(mCurrentLocation[0])) * Math.cos(angularDistance) +
                                            Math.cos(Math.toRadians(mCurrentLocation[0])) * Math.sin(angularDistance) *
                                            Math.cos(bearing));

        float longitude = (float) (Math.toRadians(mCurrentLocation[1]) +
                                   Math.atan2( Math.sin(bearing) * Math.sin(angularDistance) * Math.cos(Math.toRadians(mCurrentLocation[0])),
                                              Math.cos(angularDistance) - Math.sin(Math.toRadians(mCurrentLocation[0])) * Math.sin(latitude)));

        newPosition[0] = (float) Math.toDegrees(latitude);
        newPosition[1] = (float)(Math.toDegrees(longitude));

        System.out.println("Latitude         = " + newPosition[0]);
        System.out.println("Longitude        = " + newPosition[1]);
        System.out.println("bearing          = " + bearing*(180/Math.PI));
        System.out.println("nbre pas         = " + nbrePas);

        return newPosition;
    }

    public void start ( ) {
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        orientation.start();
    }

    public void stop ( ) {
        sensorManager.unregisterListener(this);
        orientation.stop();
    }

    private Orientation.OrientationListener orientationListener = new Orientation.OrientationListener ( ) {
        @Override
        public void onOrientationChanged(float cap) {
            capPas = cap;
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            // Calcul de la norme de l'acceleration exterieur
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float acceleration = (float) (Math.sqrt(x*x + y*y + z*z) - 9.805);

            Date now = new Date();

            if (acceleration>=seuil && detectPas) {
                mCurrentLocation = computeNextStep(taillePas,capPas);
                accelerometerListener.mvtChangedDetected(mCurrentLocation);
                nbrePas += 1;
                detectPas = false;
                detectTime = new Date();
            } else if (acceleration<seuil && now.getTime()-detectTime.getTime()>500) {
                detectPas = true;
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setLatitude(float latitude) {
        mCurrentLocation[0] = latitude;
    }

    public void setLongitude(float longitude) {
        mCurrentLocation[1] = longitude;
    }

    public void setAccelerometerListener(AccelerometerListener accelerometerListener) {
        this.accelerometerListener = accelerometerListener;
    }

    public interface AccelerometerListener {
        public void mvtChangedDetected(float[] position);
    }

}
