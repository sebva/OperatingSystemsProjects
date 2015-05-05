package ch.unine.os.as4;

/**
 * Journey between two stands. Immutable object.
 *
 * @author Sébastien Vaucher
 */
public class Journey {
    /**
     * ID of the stand where the journey started
     */
    public final int startStandId;
    /**
     * ID of the stand where the journey ended
     */
    public final int endStandId;

    /**
     * Create a new journey
     * @param startStandId The ID of the stand where the journey started
     * @param endStandId The ID of the stand where the journey ended
     */
    public Journey(int startStandId, int endStandId) {
        this.startStandId = startStandId;
        this.endStandId = endStandId;
    }
}
