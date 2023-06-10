package com.example.enviromentplantdetection;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
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
    private TextView tvRain;
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
        lottieWeather = findViewById(R.id.lottieWeather);
        btnWaterPump = findViewById(R.id.btnWaterPump);

        // Set up click listener for the water pump button
        btnWaterPump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the water pump state and update the button text accordingly
                isWaterPumpOn = !isWaterPumpOn;
                updateWaterPumpButton();
                FirebaseDatabase.getInstance().getReference("sensor_data").child("isWaterPumpOn").setValue(isWaterPumpOn);
            }
        });

        // Retrieve data from Firebase Realtime Database
        FirebaseDatabase.getInstance().getReference("sensor_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Update UI with new sensor data
                double temperature = dataSnapshot.child("temperature").getValue(Double.class);
                double humidity = dataSnapshot.child("humidity").getValue(Double.class);
                double rainPercentage = dataSnapshot.child("rainPercentage").getValue(Double.class);
                isWaterPumpOn = dataSnapshot.child("isWaterPumpOn").getValue(Boolean.class);

                tvTemperature.setText(String.valueOf(temperature));
                tvHumidity.setText(String.valueOf(humidity));
                tvRain.setText(String.valueOf(rainPercentage));
                updateWaterPumpButton();
                updateWeatherAnimation(rainPercentage);
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
        }else if (rainPercentage > 30) {
            animateWeather(R.raw.anim_overcast);
        }else {
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
