package parkitny.de.bluetoothmyfit.services;

/**
 * Created by Sebastian on 21.03.2016.
 */
public class HeartRateEvent {

    private int heartRate;

    public HeartRateEvent(int heartRate) {
        this.heartRate = heartRate;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }
}
