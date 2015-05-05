package ch.unine.os.as4;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bike stand where people can rent or return bikes
 * <p/>
 * Synchronization logic is in this class
 *
 * @author Sébastien Vaucher
 */
public class BikeStand {
    public static final int STAND_MAX_CAPACITY = 10;
    public static final int STAND_IDEAL_CAPACITY = 5;

    private final int standId;

    /**
     * Number of bikes currently available at the stand. Write only allowed in possession of the mutex.
     * <p/>
     * Consistency rule: 0 &gt;= bikeCount &gt;= STAND_MAX_CAPACITY
     */
    private final AtomicInteger bikeCount;
    /**
     * List of bikes available at this stand. Read/write allowed only in possession of the mutex.
     */
    private final Queue<Bike> bikeList;

    /*
    Attributes for synchronization. Each User who wants to rent or return a bike has to first take a ticket for the
    queue corresponding to the action. Tickets ensure FIFO order. Renters and returners queue separately, because they
    can each unblock the other queue. The mutex attribute ensures that no two threads modify the available bikes
    concurrently.

    When the truck comes in, he sets both current ticket variables to 0, as no one has this ticket number. He then
    acquires the mutex, waiting for the current user to finish his action. After the balancing operation is completed,
    the truck sets the current ticket variables back to their original values.
     */
    private final AtomicInteger rentTicket;
    private final AtomicInteger rentCurrentTicket;
    private final AtomicInteger returnTicket;
    private final AtomicInteger returnCurrentTicket;
    private final Lock mutex;

    /*
    If the balancing truck modified the current ticket values while a user was performing an operation, then it is the
    truck's duty to increment them after it finishes the balancing operation.
     */
    private volatile boolean rentCurrentTicketNeedsIncrement = false;
    private volatile boolean returnCurrentTicketNeedsIncrement = false;

    /**
     * Create a new bike stand
     *
     * @param standId ID of the stand, it is crucial that each stand has a unique ID number corresponding to its
     *                position in the stands list in City.
     * @param city A reference on the City object
     */
    public BikeStand(City city, int standId) {
        this.standId = standId;
        this.bikeCount = new AtomicInteger(STAND_IDEAL_CAPACITY);
        this.bikeList = new ArrayDeque<Bike>(STAND_MAX_CAPACITY);
        // Create bikes
        for (int i = 0; i < STAND_IDEAL_CAPACITY; i++) {
            bikeList.add(new Bike(city));
        }

        // Ticket #0 is for the truck, so start at 1
        this.rentTicket = new AtomicInteger(1);
        this.rentCurrentTicket = new AtomicInteger(1);
        this.returnTicket = new AtomicInteger(1);
        this.returnCurrentTicket = new AtomicInteger(1);

        this.mutex = new ReentrantLock();
    }

    /**
     * Rent a bike; when the method call returns, the bike is rented (blocking operation).
     *
     * @return The rented bike
     */
    public Bike rentBike() {
        // Take a ticket
        int ticketId = rentTicket.getAndIncrement();
        boolean done = false;
        Bike bike = null;

        while (!done) {
            /*
            Lurking stage: wait until our ticket is called and a bike is available.
            More efficient than acquiring the mutex for nothing.
             */
            while (rentCurrentTicket.get() != ticketId || bikeCount.get() == 0) ;

            mutex.lock();
            try {
                // Ensure that the conditions to rent a bike are met, otherwise go back to lurking stage
                if (rentCurrentTicket.get() == ticketId && bikeCount.get() > 0) {
                    // Get the bike
                    bikeCount.decrementAndGet();
                    bike = bikeList.remove();
                    bike.startJourney(standId);
                    done = true;

                    /*
                    Use compare and set to ensure that the current ticket number is correct before incrementing it. The
                    truck may have set the current ticket number to 0 in the meantime. In this case, operation will fail.
                    We then need to notify the truck thread to do it.
                    */
                    if (!rentCurrentTicket.compareAndSet(ticketId, ticketId + 1)) {
                        rentCurrentTicketNeedsIncrement = true;
                    }
                }
            } finally {
                mutex.unlock(); // Unlock always in finally block
            }
        }

        return bike;
    }

    /**
     * Return a bike; when the method call returns, the bike is returned (blocking operation).
     *
     * @param bike The bike to return
     */
    public void returnBike(Bike bike) {
        // (Method logic is equivalent to rent operation)

        // Take a ticket
        int ticketId = returnTicket.getAndIncrement();
        boolean done = false;

        while (!done) {
            /*
            Lurking stage: wait until our ticket is called and a bike is available.
            More efficient than acquiring the mutex for nothing.
             */
            while (returnCurrentTicket.get() != ticketId || bikeCount.get() >= STAND_MAX_CAPACITY) ;

            mutex.lock();
            try {
                // Ensure that the conditions to return a bike are met, otherwise go back to lurking stage
                if (returnCurrentTicket.get() == ticketId && bikeCount.get() < STAND_MAX_CAPACITY) {
                    bike.endJourney(standId);
                    bikeList.add(bike);
                    bikeCount.incrementAndGet();
                    done = true;

                    /*
                    Use compare and set to ensure that the current ticket number is correct before incrementing it. The
                    truck may have set the current ticket number to 0 in the meantime. In this case, operation will fail.
                    We then need to notify the truck thread to do it.
                    */
                    if (!returnCurrentTicket.compareAndSet(ticketId, ticketId + 1)) {
                        returnCurrentTicketNeedsIncrement = true;
                    }
                }
            } finally {
                mutex.unlock(); // Unlock always in finally block
            }
        }
    }

    /**
     * Called by the balancing truck when it needs to normalize the number of bikeCount in the stand.
     *
     * @param truck The balancing truck object
     */
    public void refillStand(BalancingTruck truck) {
        // Set current ticket to 0 (no one has this number)
        int oldRentTicket = rentCurrentTicket.getAndSet(0);
        int oldReturnTicket = returnCurrentTicket.getAndSet(0);

        mutex.lock();
        try {
        /*
        If we set the current ticket to 0 while a user was finishing his operation, the duty to increment the counter
        is ours. The consistency of NeedsIncrement variables is ensured by the mutex.
        */
            if (rentCurrentTicketNeedsIncrement) {
                oldRentTicket++;
            }
            if (returnCurrentTicketNeedsIncrement) {
                oldReturnTicket++;
            }
            rentCurrentTicketNeedsIncrement = returnCurrentTicketNeedsIncrement = false;

            truck.balanceStand(bikeCount, bikeList);
        } finally {
            mutex.unlock();
        }

        // Call the next user of each queue
        rentCurrentTicket.set(oldRentTicket);
        returnCurrentTicket.set(oldReturnTicket);
    }

    /**
     * Get the unique ID of this stand
     * @return The unique ID
     */
    public int getStandId() {
        return standId;
    }

    /**
     * Get the number of available bikes in this stand
     * @return The number of bikes that can be rented
     */
    public int getBikeCount() {
        return bikeCount.get();
    }
}