package ch.unine.os.as4;

import java.util.ArrayList;
import java.util.List;

/**
 * Bike-rental system of a city.
 *
 * Has a reference to every other object of the system.
 *
 * @author Sébastien Vaucher
 */
public class City {
    // Probability lists given in the subject description
    private static final int[] startStands = {0, 0, 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 5};
    private static final int[] arrivalStands = {0, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 5};

    private List<User> users;
    private List<BikeStand> stands;
    private BalancingTruck truck;
    /**
     * Flag that tells the truck when to stop running
     */
    private volatile boolean running;

    /**
     * Create a new bike rental system
     * @param nbUsers Number of users (threads) renting bikes
     * @param nbStands Number of available bike stands
     */
    public City(int nbUsers, int nbStands) {
        // Assert that the probability lists are correct for the input
        if (startStands[startStands.length - 1] != nbStands - 1 ||
                arrivalStands[arrivalStands.length - 1] != nbStands - 1) {
            throw new RuntimeException("Invalid probability list for the given number of stands");
        }

        users = new ArrayList<User>(nbUsers);
        stands = new ArrayList<BikeStand>(nbStands);

        for (int i = 0; i < nbUsers; i++) {
            users.add(new User(this, i));
        }
        for (int i = 0; i < nbStands; i++) {
            stands.add(new BikeStand(i));
        }

        truck = new BalancingTruck(this);
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Run the simulation, results are printed to stdout
     */
    public void runSimulation() {
        running = true;
        truck.start();

        for (User user : users) {
            user.start();
        }
        for (User user : users) {
            try {
                user.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        running = false;
        truck.interrupt();

        // Print final state
        for (BikeStand stand : stands) {
            System.out.println(stand);
        }
        System.out.println(truck);
    }

    /**
     * Get a random stand to start a journey
     * @return A random instance of BikeStand
     */
    public BikeStand getRandomStartStand() {
        return stands.get(getRandomStandId(startStands));
    }

    /**
     * Get a random stand to end a journey
     * @return A random instance of BikeStand
     */
    public BikeStand getRandomArrivalStand() {
        return stands.get(getRandomStandId(arrivalStands));
    }

    private int getRandomStandId(int[] source) {
        return source[(int) (Math.random() * source.length)];
    }

    /**
     * Get the stand located immediately clockwise to the given stand
     * @param stand The current stand
     * @return The stand located at the next index
     */
    public BikeStand getNextStand(BikeStand stand) {
        return stands.get(stand == null ? 0 : (stand.getStandId() + 1) % stands.size());
    }

    /**
     * Get the stand located immediately counter-clockwise to the given stand
     * @param stand The current stand
     * @return The stand located at the previous index
     */
    public BikeStand getPreviousStand(BikeStand stand) {
        int standId = 0;
        if (stand != null) {
            standId = stand.getStandId() - 1;
            if (standId < 0) {
                standId = stands.size() - 1;
            }
        }
        return stands.get(standId);
    }

    /**
     * Compute the distance between two distinct given stands
     * @param startStand The first stand
     * @param arrivalStand The second stand
     * @return The distance between both stands
     */
    public int distanceBetween(BikeStand startStand, BikeStand arrivalStand) {
        // Try clockwise
        int clockwiseCount = 0;
        BikeStand stand = startStand;
        while (stand != arrivalStand) {
            stand = getNextStand(stand);
            clockwiseCount++;
        }

        // Try counter-clockwise
        int counterClockwiseCount = 0;
        stand = startStand;
        while (stand != arrivalStand) {
            stand = getPreviousStand(stand);
            counterClockwiseCount++;
        }

        return Integer.min(clockwiseCount, counterClockwiseCount);
    }

    /**
     * Entry-point of the program
     * @param args Ignored
     */
    public static void main(String[] args) {
        City city = new City(100, 6); // Has to be nbStands = 6 for the probability lists
        city.runSimulation();
    }
}
