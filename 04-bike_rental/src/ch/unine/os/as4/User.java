package ch.unine.os.as4;

import java.util.ArrayList;
import java.util.List;

/**
 * User of the bike rental system
 *
 * @author Sébastien Vaucher
 */
public class User extends Thread {
    public static final int NB_TRIPS_MIN = 3;
    public static final int NB_TRIPS_MAX = 8;
    public static final int WAIT_BETWEEN_TRIPS_MIN = 100;
    public static final int WAIT_BETWEEN_TRIPS_MAX = 200;
    public static final int TRIP_DURATION_PER_STAND = 50;

    private final City city;
    private final int userId;
    private int tripsRemaining;

    /**
     * List of trips made by the user
     */
    private final List<Journey> journeys;

    /**
     * Create a new user of the system
     * @param city The City object
     * @param userId A unique user ID
     */
    public User(City city, int userId) {
        this.city = city;
        this.userId = userId;

        // Generate random number of trips to make
        this.tripsRemaining = RandomUtils.randomRange(NB_TRIPS_MIN, NB_TRIPS_MAX);

        this.journeys = new ArrayList<Journey>(tripsRemaining);
    }

    @Override
    public void run() {
        while (tripsRemaining --> 0) {
            try {
                Thread.sleep(RandomUtils.randomRange(WAIT_BETWEEN_TRIPS_MIN, WAIT_BETWEEN_TRIPS_MAX));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BikeStand startStand = city.getRandomStartStand();
            BikeStand arrivalStand;
            do {
                arrivalStand = city.getRandomArrivalStand();
            } while (startStand == arrivalStand); // Try again if both stands are the same

            Bike bike = startStand.rentBike();

            try {
                Thread.sleep(TRIP_DURATION_PER_STAND * city.distanceBetween(startStand, arrivalStand));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            arrivalStand.returnBike(bike);
            journeys.add(new Journey(startStand.getStandId(), arrivalStand.getStandId()));
        }
    }

    /**
     * Get the unique ID associated to this user
     * @return A unique ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Get the list of trips made by the user
     * @return The list of trips
     */
    public List<Journey> getJourneys() {
        return journeys;
    }
}
