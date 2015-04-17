package ch.unine.os.as4;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Truck keeping the system running by balancing the number of bikes between the stands
 *
 * @author Sébastien Vaucher
 */
public class BalancingTruck extends Thread {
    public static final int TRIP_DURATION_BETWEEN_STANDS = 25;
    public static final int TRUCK_CAPACITY = BikeStand.STAND_MAX_CAPACITY;

    private final City city;
    /**
     * Number of bikes currently loaded on the truck
     */
    private volatile int truckBikes = 0;

    public BalancingTruck(City city) {
        this.city = city;
    }

    @Override
    public void run() {
        BikeStand stand = city.getNextStand(null);
        while (city.isRunning()) {
            stand.refillStand(this);
            try {
                Thread.sleep(TRIP_DURATION_BETWEEN_STANDS);
            } catch (InterruptedException ignored) {
            }
            stand = city.getNextStand(stand);
        }
    }

    /**
     * Try to load/unload bikes on/from the truck in order to have the number of bikes in the given stand as close as
     * STAND_IDEAL_CAPACITY as possible.
     * @param standBikes A reference on the AtomicInteger object representing the number of bikes in the stand
     */
    public void balanceStand(AtomicInteger standBikes) {
        int initial = standBikes.get();
        // Stand has too much bikes and truck is not full
        while (standBikes.get() > BikeStand.STAND_IDEAL_CAPACITY && truckBikes < TRUCK_CAPACITY) {
            standBikes.decrementAndGet();
            truckBikes++;
        }
        // Stand needs more bikes and truck is not empty
        while (standBikes.get() < BikeStand.STAND_IDEAL_CAPACITY && truckBikes > 0) {
            standBikes.incrementAndGet();
            truckBikes--;
        }
        System.out.println("Truck: refilled to " + standBikes.get() + ", was " + initial);
    }

    @Override
    public String toString() {
        return "BalancingTruck{" +
                "truckBikes=" + truckBikes +
                '}';
    }
}
