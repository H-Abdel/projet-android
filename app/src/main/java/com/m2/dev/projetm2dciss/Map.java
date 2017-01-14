package com.m2.dev.projetm2dciss;

import android.content.Context;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class Map extends AppCompatActivity {

    private MapView mapView;
    private Marker marque;

    private PDR pdr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, getString(R.string.access_token));
        setContentView(R.layout.activity_map);

        // Create a mapView
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Add a MapBoxMap
        mapView.getMapAsync(new OnMapReadyCallback() {

            MarkerViewOptions markerViewOptions;

            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                // Si le marqueur n'existe pas encore on le créé
                if (marque == null) {
                    markerViewOptions = new MarkerViewOptions().position(new LatLng(45.19283, 5.77366));
                    mapboxMap.addMarker(markerViewOptions);
                    marque = mapboxMap.getMarkers().get(0);
                }

                // Positionne le marqueur là où on clique
                mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng point) {
                        marque.setPosition(new LatLng(point.getLatitude(), point.getLongitude()));
                        pdr.setLatitude((float)point.getLatitude());
                        pdr.setLongitude((float)point.getLongitude());
                    }
                });

            }
        });

        // Initialisation du pdr
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pdr = new PDR(sensorManager);
        pdr.setAccelerometerListener(accelerometerListener);

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        pdr.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        pdr.stop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    private PDR.AccelerometerListener accelerometerListener = new PDR.AccelerometerListener() {
        @Override
        public void mvtChangedDetected(float[] position) {
            marque.setPosition(new LatLng(position[0], position[1]));
        }
    };
}
