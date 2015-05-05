package ch.unine.os.as4;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bike that can be rented by users or moved by the balancing truck
 *
 * @author Sébastien Vaucher
 */
public class Bike {
    /**
     * Each instance gets an ID from this
     */
    private static final AtomicInteger nextId = new AtomicInteger(0);
    private final int id;
    /**
     * List of trips done by this bike
     */
    private final List<Journey> journeys;

    private int currentJourneyStartStandId;

    /**
     * Create a new bike
     * @param city A reference to the city this bike belongs to
     */
    public Bike(City city) {
        this.id = nextId.getAndIncrement();
        this.journeys = new LinkedList<Journey>();

        city.registerBike(this);
    }

    /**
     * Notify of a new journey
     * @param standId The identifier of the stand from which the bike was rented
     */
    public void startJourney(int standId) {
        currentJourneyStartStandId = standId;
    }

    /**
     * Notify the end of a journey
     * @param standId The identifier of the stand where the bike is returned
     */
    public void endJourney(int standId) {
        journeys.add(new Journey(currentJourneyStartStandId, standId));
    }

    /**
     * Get the unique identifier of this instance
     * @return Unique ID
     */
    public int getBikeId() {
        return id;
    }

    /**
     * Get a list of all trips made by this bike
     * @return A list of Journey objects. Do not attempt to modify the list!
     */
    public List<Journey> getJourneys() {
        return journeys;
    }
}
