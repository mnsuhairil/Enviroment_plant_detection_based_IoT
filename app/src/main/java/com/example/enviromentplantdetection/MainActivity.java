package com.example.enviromentplantdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView tvTemperature;
    private TextView tvHumidity;
    private TextView tvRain,tvDate;
    private LottieAnimationView lottieWeather;
    private Button btnWaterPump;

    private boolean isWaterPumpOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvRain = findViewById(R.id.tvRain);
        tvDate = findViewById(R.id.tvDate);

        lottieWeather = findViewById(R.id.lottieWeather);
        btnWaterPump = findViewById(R.id.btnWaterPump);

        LinearLayout temperaturePanel = findViewById(R.id.temperaturePanel);
        LinearLayout humidityPanel = findViewById(R.id.humidityPanel);
        LinearLayout rainPanel = findViewById(R.id.rainPanel);
        temperaturePanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GraphActivity.class);
                intent.putExtra("sensorType", "temperature");
                startActivity(intent);
            }
        });
        humidityPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GraphActivity.class);
                intent.putExtra("sensorType", "humidity");
                startActivity(intent);
            }
        });
        rainPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GraphActivity.class);
                intent.putExtra("sensorType", "rainpercentage");
                startActivity(intent);
            }
        });
        // Set up click listener for the water pump button
        btnWaterPump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the water pump state and update the button text accordingly
                isWaterPumpOn = !isWaterPumpOn;
                updateWaterPumpButton();
                FirebaseDatabase.getInstance().getReference("WaterPumpControl").child("isWaterPumpOn").setValue(isWaterPumpOn);
            }
        });

        // Retrieve data from Firebase Realtime Database
        FirebaseDatabase.getInstance().getReference("SensorData").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Update UI with new sensor data
                String temperature = dataSnapshot.child("temperature").getValue(String.class);
                String humidity = dataSnapshot.child("humidity").getValue(String.class);
                String rainPercentage = dataSnapshot.child("rainpercentage").getValue(String.class);
                isWaterPumpOn = Boolean.TRUE.equals(dataSnapshot.child("WaterPumpControl").child("isWaterPumpOn").getValue(Boolean.class));

                tvTemperature.setText(temperature + " °C");
                tvHumidity.setText(humidity + "% RH");
                tvRain.setText(rainPercentage + "%");
                getTime();
                updateWaterPumpButton();
                updateWeatherAnimation(Double.parseDouble(rainPercentage));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });

    }

    private void getTime() {
        FirebaseDatabase.getInstance().getReference("SystemData").child("systemTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Update UI with new sensor data
                String days = dataSnapshot.child("days").getValue(String.class);
                String hours = dataSnapshot.child("hours").getValue(String.class);
                String minutes = dataSnapshot.child("minutes").getValue(String.class);
                String seconds = dataSnapshot.child("seconds").getValue(String.class);

                tvDate.setText("System Operation Time\n"+"Day "+days+"\n"+hours+":"+minutes+":"+seconds );

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }


    private void updateWaterPumpButton() {
        if (isWaterPumpOn) {
            btnWaterPump.setText("Water Pump: ON");
        } else {
            btnWaterPump.setText("Water Pump: OFF");
        }
    }

    private void updateWeatherAnimation(double rainPercentage) {
        if (rainPercentage > 70) {
            animateWeather(R.raw.anim_rain);
        } else if (rainPercentage > 30) {
            animateWeather(R.raw.anim_overcast);
        } else {
            animateWeather(R.raw.anim_sun);
        }
    }

    private void animateWeather(int animationRes) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(lottieWeather, "alpha", 1f, 0f);
        fadeOut.setDuration(500);
        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                lottieWeather.setAnimation(animationRes);
                lottieWeather.setRepeatCount(LottieDrawable.INFINITE);
                lottieWeather.playAnimation();
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(lottieWeather, "alpha", 0f, 1f);
                fadeIn.setDuration(500);
                fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
                fadeIn.start();
            }
        });

        fadeOut.start();
    }
}
