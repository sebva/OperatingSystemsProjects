package ch.unine.os.as4;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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

    private final List<User> users;
    private final List<BikeStand> stands;
    private final BalancingTruck truck;
    private final List<Bike> bikes;

    /**
     * Flag that tells the truck when to stop running. Volatile to ensure that the other thread sees concurrent
     * modifications.
     */
    private volatile boolean running;

    /**
     * Create a new bike rental system
     * @param nbUsers Number of users (threads) renting bikes
     * @param nbStands Number of available bike stands
     */
    public City(int nbUsers, int nbStands) {
        System.out.println(String.format("%d users, %d stands, %d bikes/stand", nbUsers, nbStands,
                                                                                BikeStand.STAND_IDEAL_CAPACITY));

        // Assert that the probability lists are correct for the input
        if (startStands[startStands.length - 1] != nbStands - 1 ||
                arrivalStands[arrivalStands.length - 1] != nbStands - 1) {
            throw new RuntimeException("Invalid probability list for the given number of stands");
        }

        // Create objects of the system
        users = new ArrayList<User>(nbUsers);
        stands = new ArrayList<BikeStand>(nbStands);
        bikes = new ArrayList<Bike>(BikeStand.STAND_IDEAL_CAPACITY * nbStands);
        for (int i = 0; i < nbUsers; i++) {
            users.add(new User(this, i));
        }
        for (int i = 0; i < nbStands; i++) {
            stands.add(new BikeStand(this, i));
        }
        truck = new BalancingTruck(this);
    }

    /**
     * Is there at least 1 user that is using the system. Used to stop the balancing truck thread.
     * @return True if the system is still running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Run the simulation, Results are printed to stdout.
     */
    public void runSimulation() {
        long startTime = System.nanoTime();

        running = true;
        truck.start();

        for (User user : users) {
            user.start();
        }
        System.out.println("Simulation started");

        for (User user : users) {
            try {
                user.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Stop the truck thread
        running = false;
        truck.interrupt(); // Force exit of a wait() call

        long elapsed = System.nanoTime() - startTime;

        System.out.println("End of simulation\n");

        // Print trips of bikes, users and truck
        for (Bike bike : bikes) {
            System.out.println(String.format("Bike %d trips {", bike.getBikeId()));
            for (Journey journey : bike.getJourneys()) {
                System.out.println(String.format("\t%d --> %d", journey.startStandId, journey.endStandId));
            }
            System.out.println("}");
        }
        for (User user : users) {
            System.out.println(String.format("User %d trips {", user.getUserId()));
            for (Journey journey : user.getJourneys()) {
                System.out.println(String.format("\t%d --> %d", journey.startStandId, journey.endStandId));
            }
            System.out.println("}");
        }
        int standId = 0;
        int inTruck = 0;
        System.out.println("Truck refills {");
        NumberFormat refillFormat = new DecimalFormat("+#;-#");
        for (int refill : truck.getRefillHistory()) {
            inTruck -= refill;
            System.out.println(String.format("\tStand %d: %s bike(s); %d bike(s) in truck", standId, refillFormat.format(refill), inTruck));
            standId = (standId + 1) % stands.size();
        }
        System.out.println("}");

        // Print final state
        System.out.println("\nFinal state {");
        for (BikeStand stand : stands) {
            System.out.println(String.format("\tStand %d: %d bike(s)", stand.getStandId(), stand.getBikeCount()));
        }
        System.out.println(String.format("\tTruck:   %d bike(s)\n}", truck.getBikeCount()));
        System.out.println(String.format("Time elapsed in simulation: %.2f ms", elapsed / 1000000.));
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

        // Return shortest distance
        return Integer.min(clockwiseCount, counterClockwiseCount);
    }

    /**
     * Register a reference to a Bike object on its creation
     * @param bike The Bike to keep the reference to
     */
    public void registerBike(Bike bike) {
        bikes.add(bike);
    }

    /**
     * Entry-point of the program
     * @param args Ignored
     */
    public static void main(String[] args) {
        City city = new City(200, 6); // Has to be nbStands = 6 for the probability lists
        city.runSimulation();
    }
}
