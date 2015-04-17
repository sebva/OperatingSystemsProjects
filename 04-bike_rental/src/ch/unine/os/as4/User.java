package ch.unine.os.as4;

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
     * Create a new user in the system
     * @param city The City object
     * @param userId A unique user ID, used only for logging
     */
    public User(City city, int userId) {
        this.city = city;
        this.userId = userId;
        this.tripsRemaining = RandomUtils.randomRange(NB_TRIPS_MIN, NB_TRIPS_MAX);
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

            startStand.rentBike();

            try {
                Thread.sleep(TRIP_DURATION_PER_STAND * city.distanceBetween(startStand, arrivalStand));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            arrivalStand.returnBike();

            System.out.println("User " + userId + " has started at " + startStand.getStandId() + " and arrived at " + arrivalStand.getStandId());
        }
    }
}
