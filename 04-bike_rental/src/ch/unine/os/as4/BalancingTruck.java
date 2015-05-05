package ch.unine.os.as4;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
    private volatile int truckBikeCount = 0;
    /**
     * Queue of bikes stored on the truck
     */
    private final Queue<Bike> truckBikeList;
    /**
     * List containing each refill done by the truck. A positive value means that the truck was unloaded.
     */
    private final List<Integer> refillHistory;

    public BalancingTruck(City city) {
        this.city = city;
        truckBikeList = new ArrayDeque<Bike>(TRUCK_CAPACITY);
        refillHistory = new LinkedList<Integer>();
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
     * STAND_IDEAL_CAPACITY as possible. Only the BikeStand class can call this method. It has to ensure the
     * BalancingTruck is executing alone.
     * @param standBikeCount A reference on the AtomicInteger object representing the number of bikes in the stand
     * @param standBikeList A reference on the list of bikes in the stand
     */
    public void balanceStand(AtomicInteger standBikeCount, Queue<Bike> standBikeList) {
        int initial = standBikeCount.get();

        // Stand has too much bikes and truck is not full
        while (standBikeCount.get() > BikeStand.STAND_IDEAL_CAPACITY && truckBikeCount < TRUCK_CAPACITY) {
            truckBikeList.add(standBikeList.remove());

            standBikeCount.decrementAndGet();
            truckBikeCount++;
        }
        // Stand needs more bikes and truck is not empty
        while (standBikeCount.get() < BikeStand.STAND_IDEAL_CAPACITY && truckBikeCount > 0) {
            standBikeList.add(truckBikeList.remove());

            standBikeCount.incrementAndGet();
            truckBikeCount--;
        }

        refillHistory.add(standBikeCount.get() - initial);
    }

    /**
     * Get the number of bikes in the truck
     * @return The number of bikes in the truck
     */
    public int getBikeCount() {
        return truckBikeCount;
    }

    /**
     * Get the history of refills of stands
     * @return A list containing, for each stand, the number of bikes added or removed from the stand.
     */
    public List<Integer> getRefillHistory() {
        return refillHistory;
    }
}
