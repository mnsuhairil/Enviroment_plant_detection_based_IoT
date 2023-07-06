package com.example.enviromentplantdetection;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class GraphActivity extends AppCompatActivity {
    private GraphView graphView;
    private String sensorType;
    int i = 0;
    private int maxCount = 0;
    private double maxData = 0;
    private double minData = 9999;
    private TextView dateTextView, pointXTextView, pointYTextView,textView;
    private LinearLayout dataInfoLinearLayout;
    private FrameLayout graphFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        graphView = findViewById(R.id.graph);
        graphFrameLayout = findViewById(R.id.graphFrameLayout);
        graphView.getGridLabelRenderer().setGridColor(Color.DKGRAY);

        graphView.getViewport().setScalable(true);  // Enable scaling and scrolling
        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setScalableY(true);  // Enable vertical scaling and scrolling
        graphView.getViewport().setScrollableY(true);
        textView = findViewById(R.id.text1);
        dateTextView = findViewById(R.id.dateTextView);
        pointXTextView = findViewById(R.id.pointXTextView);
        pointYTextView = findViewById(R.id.pointYTextView);
        dataInfoLinearLayout = findViewById(R.id.dataInfoLinearLayout);

        Intent intent = getIntent();
        if (intent != null) {
            sensorType = intent.getStringExtra("sensorType");
            if (sensorType.equals("temperature")){
                textView.setText("Temperature Data");
            }

            else if (sensorType.equals("humidity")){
                textView.setText("Humidity Data");
            }else {
                textView.setText("Rain Percentage Data");
            }
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("DataLogs");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                processAndDisplayData(dataSnapshot);
                updateViewportBounds();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Handle data changes if necessary
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Handle data removal if necessary
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Handle data movement if necessary
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        // Create a gradient drawable for the frame background
        GradientDrawable frameDrawable = new GradientDrawable();
        frameDrawable.setShape(GradientDrawable.RECTANGLE);
        frameDrawable.setStroke(10, Color.WHITE);
        frameDrawable.setCornerRadius(20);
        frameDrawable.setColor(Color.BLACK);

        // Apply the frame drawable to the graph frame layout
        graphFrameLayout.setBackground(frameDrawable);

        // Set title for X and Y axes
        graphView.getGridLabelRenderer().setHorizontalAxisTitle("Total Data");

    }

    private void processAndDisplayData(DataSnapshot dataSnapshot) {
        // Create lists for humidity and temperature data
        List<DataPoint> humidityData = new ArrayList<>();
        String date = dataSnapshot.getKey();
        System.out.println("dates " + date);

        for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
            String dateChildren = dateSnapshot.getKey();
            System.out.println("dateChildren " + dateChildren);
            if (dateChildren.equals(sensorType)) {
                // Retrieve humidity data for the current date
                DataSnapshot humiditySnapshot = dateSnapshot.child(sensorType);
                System.out.println("test 1 " + humiditySnapshot);

                for (DataSnapshot snapshot : dateSnapshot.getChildren()) {
                    System.out.println("test 2 " + snapshot);
                    String temperatureCount = snapshot.getKey(); // Get the unique key value
                    if (!temperatureCount.equals("")) {
                        temperatureCount = String.valueOf(i);
                        i++;
                    }
                    System.out.println("test 3 " + temperatureCount);
                    double value = Double.parseDouble(Objects.requireNonNull(snapshot.getValue()).toString());
                    System.out.println("humidity value " + value);
                    humidityData.add(new DataPoint(Double.parseDouble(temperatureCount), value));

                    // Update maxData if the current value is higher
                    if (value > maxData) {
                        maxData = value;
                    }
                    // Update minData if the current value is lower
                    if (value < minData) {
                        minData = value;
                    }
                }
            }
        }

        // Sort the humidity data based on the x-values in ascending order
        humidityData.sort(new Comparator<DataPoint>() {
            @Override
            public int compare(DataPoint dataPoint1, DataPoint dataPoint2) {
                return Double.compare(dataPoint1.getX(), dataPoint2.getX());
            }
        });

        // Create and configure the series for humidity
        LineGraphSeries<DataPoint> humiditySeries = new LineGraphSeries<>(humidityData.toArray(new DataPoint[0]));
        humiditySeries.setDrawDataPoints(true);
        humiditySeries.setDataPointsRadius(5);
        humiditySeries.setColor(Color.BLUE);

        // Add the series to the graph
        graphView.addSeries(humiditySeries);

        humiditySeries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                // Update the TextView with the tapped data point values

                dateTextView.setText("Date: " + date);
                pointXTextView.setText("X: " + dataPoint.getX());
                pointYTextView.setText("Y: " + dataPoint.getY());
                dataInfoLinearLayout.setVisibility(View.VISIBLE);
            }

        });

        if (i > maxCount) {
            maxCount = i;
        }
    }

    private void updateViewportBounds() {
        // Set initial viewport boundaries to cover the entire graph data range
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(maxCount);
        System.out.println("maxc " + maxCount);
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(minData);
        System.out.println("mind " + minData);
        graphView.getViewport().setMaxY(maxData);
        System.out.println("maxd " + maxData);
    }
}
