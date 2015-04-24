package provided;

/**
 * Your TLB class must implement this interface.
 */
public interface TLB_interface {
	/**
	 * Checks if the association between virtualPage and a physical frame is stored in the TLB.
	 * If the association is here, returns the physical frame number and set the 'used' bit to true (for the clock/second chance replacement algorithm).
	 * It the association is not here, returns -1
	 *  
	 * @param processIdentifier
	 * @param virtualPage
	 * @return physical frame if association stored in TLB, -1 if association is not stored
	 */
	public int getAssociation (int processIdentifier, int virtualPage);
	
	/**
	 * Register an entry in the TLB. If the TLB is full, the clock/second chance algorithm must be used to determine which entry to discard. 
	 * 
	 * @param processIdentifier
	 * @param virtualPage
	 * @param physicalFrame
	 */
	public void setAssociation(int processIdentifier, int virtualPage, int physicalFrame);
	
	/**
	 * Removes an entry in the TLB. 
	 * 
	 * @param processIdentifier
	 * @param virtualPage
	 */
	public void removeAssociation (int processIdentifier, int virtualPage);
	
	/**
	 * Returns the hit rate, that is, the proportion of calls to getAssociation that were successful since the beginning of the use of this TLB.
	 * @return The hit rate.
	 */
	public float getHitRate();
}
