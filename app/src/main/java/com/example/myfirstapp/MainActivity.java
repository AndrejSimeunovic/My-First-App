package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Button startButton;
    private Button stopButton;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    private Vibrator vibrator;

    private boolean isTiltUp = false;
    private boolean isTiltDown = false;

    private MediaPlayer startAudio;
    private MediaPlayer stopAudio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(view -> startAccelerometer());

        stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(view -> stopAccelerometer());

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        startAudio = MediaPlayer.create(this, R.raw.start);
        stopAudio = MediaPlayer.create(this, R.raw.stop);
    }

    private void startAccelerometer() {
        startAudio.start();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stopAccelerometer() {
        stopAudio.start();
        sensorManager.unregisterListener(this);
    }

    private void startShakeDetector(float x, float y, float z) {
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta;
        if (mAccel > 12) {
            Toast.makeText(getApplicationContext(), "Shake event detected", Toast.LENGTH_SHORT).show();
            addVibrator();
        }
    }

    private void addVibrator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                    VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
            );
        } else {
            vibrator.vibrate(1000);
        }
    }

    private void startTiltDetector(float y, float z) {
        double tiltAngleX = Math.toDegrees(Math.atan2(y, z));
        if (tiltAngleX > 10 && !isTiltUp) {
            Toast.makeText(getApplicationContext(), "Tilted up!", Toast.LENGTH_SHORT).show();
            isTiltUp = true;
            isTiltDown = false;
        } else if (tiltAngleX < -10 && !isTiltDown) {
            Toast.makeText(getApplicationContext(), "Tilted down!", Toast.LENGTH_SHORT).show();
            isTiltDown = true;
            isTiltUp = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            ((TextView) findViewById(R.id.txt)).setText("X: " + x + ", Y: " + y + ", Z: " + z);

            startShakeDetector(x, y, z);
            startTiltDetector(y, z);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Do nothing
    }
}
