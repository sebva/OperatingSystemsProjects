package ch.unine.os.as4;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bike stand where people can rent or return bikes
 *
 * @author Sébastien Vaucher
 */
public class BikeStand {
    public static final int STAND_MAX_CAPACITY = 10;
    public static final int STAND_IDEAL_CAPACITY = 5;

    private final int standId;

    /**
     * Number of bikes currently available at the stand.
     * <p/>
     * Consistency rule: 0 >= bikes >= STAND_MAX_CAPACITY
     */
    private final AtomicInteger bikes;

    /*
    Attributes for synchronization. Each User who wants to rent or return a bike has to first take a ticket for the
    queue corresponding to the action. Tickets ensure FIFO order. Renters and returners queue separately, because they
    can each unblock the other queue. The mutex attribute ensures that no two threads modify the number of available
    bikes concurrently.

    When the truck comes in, he sets both current ticket variables to 0, as no one has this ticket number. He then
    acquires the mutex, waiting for the current user to finish his action. After the balancing operation is completed,
    the truck sets the current ticket variables back to their original values.
     */
    private final AtomicInteger rentTicket;
    private final AtomicInteger rentCurrentTicket;
    private final AtomicInteger returnTicket;
    private final AtomicInteger returnCurrentTicket;
    private final Lock mutex;

    private volatile boolean rentCurrentTicketNeedsIncrement = false;
    private volatile boolean returnCurrentTicketNeedsIncrement = false;

    /**
     * Create a new bike stand
     *
     * @param standId ID of the stand, it is crucial that each stand has a unique ID number corresponding to its
     *                position in the stands list in City.
     */
    public BikeStand(int standId) {
        this.standId = standId;
        this.bikes = new AtomicInteger(STAND_IDEAL_CAPACITY);

        // Ticket #0 is for the truck
        this.rentTicket = new AtomicInteger(1);
        this.rentCurrentTicket = new AtomicInteger(1);
        this.returnTicket = new AtomicInteger(1);
        this.returnCurrentTicket = new AtomicInteger(1);

        this.mutex = new ReentrantLock();
    }

    /**
     * Rent a bike; when the method call returns, the bike is rented (blocking operation).
     */
    public void rentBike() {
        // Take a ticket
        int ticketId = rentTicket.getAndIncrement();
        boolean done = false;

        while (!done) {
            // Lurking stage: wait until our ticket is called and a bike is available
            while (rentCurrentTicket.get() != ticketId || bikes.get() == 0) ;

            mutex.lock();
            // Ensure that the conditions to rent a bike are met, otherwise go back to lurking stage
            if (rentCurrentTicket.get() == ticketId && bikes.get() > 0) {
                bikes.decrementAndGet();
                done = true;

                /*
                Truck may have set it to 0 in the meantime, in this case we don't change the value now: the truck
                thread will do it later
                */
                if (!rentCurrentTicket.compareAndSet(ticketId, ticketId + 1)) {
                    rentCurrentTicketNeedsIncrement = true;
                }
            }
            mutex.unlock();
        }
    }

    /**
     * Return a bike; when the method call returns, the bike is returned (blocking operation).
     */
    public void returnBike() {
        int ticketId = returnTicket.getAndIncrement();
        boolean done = false;

        while (!done) {
            // Lurking stage: wait until our ticket is called and an empty space is available
            while (returnCurrentTicket.get() != ticketId || bikes.get() >= STAND_MAX_CAPACITY) ;

            mutex.lock();
            // Ensure that the conditions to return a bike are met, otherwise go back to lurking stage
            if (returnCurrentTicket.get() == ticketId && bikes.get() < STAND_MAX_CAPACITY) {
                bikes.incrementAndGet();
                done = true;

                /*
                Truck may have set it to 0 in the meantime, in this case we don't change the value now: the truck
                thread will do it later
                */
                if (!returnCurrentTicket.compareAndSet(ticketId, ticketId + 1)) {
                    returnCurrentTicketNeedsIncrement = true;
                }
            }
            mutex.unlock();
        }
    }

    /**
     * Called by the balancing truck when it needs to normalize the number of bikes in the stand
     *
     * @param truck The balancing truck object
     */
    public void refillStand(BalancingTruck truck) {
        // Set current ticket to 0 (no one has this number)
        int oldRentTicket = rentCurrentTicket.getAndSet(0);
        int oldReturnTicket = returnCurrentTicket.getAndSet(0);

        mutex.lock();
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

        truck.balanceStand(bikes);
        mutex.unlock();

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

    @Override
    public String toString() {
        return "BikeStand{" +
                "standId=" + standId +
                ", bikes=" + bikes +
                '}';
    }
}