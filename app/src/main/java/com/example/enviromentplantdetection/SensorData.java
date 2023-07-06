package com.example.enviromentplantdetection;

public class SensorData {
    private String temperature;
    private String humidity;
    private String rainPercentage;

    public SensorData() {
        // Default constructor required for Firebase
    }

    public SensorData(String temperature, String humidity, String rainPercentage) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.rainPercentage = rainPercentage;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getRainPercentage() {
        return rainPercentage;
    }

    public void setRainPercentage(String rainPercentage) {
        this.rainPercentage = rainPercentage;
    }
}
