package provided;

/**
 * This is the interface your MMU must implement to be used by the driver. 
 */
public interface MMU_Interface {
	
	// these flags help you determine if a flag is set in the first part of your page table entry
	public static final int FLAG_INVALID = 1 << 0 ;
	public static final int FLAG_USED = 1 << 1 ;
	public static final int FLAG_CLOCK_BIT = 1 << 2 ;
	public static final int FLAG_ON_DISK = 1 << 3 ;
	public static final int FLAG_DIRTY = 1 << 4 ;

	// parameters
	public static final int PAGE_SIZE = 256; // page size is the same
	public static final int NB_PAGES_IN_PROCESS_ADDRESS_SPACE = 256; // how many pages are allowed for the process (with MMU only, the actual maximum allocated memory will be less, since the first pages in memory will be occupied by the page tables.
	public static final int MAX_NUMBER_PROCESSES = 4; // the maximum number of processes supported. This parameter determines the size of the page table area at the beginning of memory.
	public static final int TLB_SIZE = 16; // how many entries in the TLB?
	
	/**
	 * Register a new process and creates the page table for it. The memory size is used to decide on the number of valid pages for this process. Other pages must be declared invalid (no physical existence).
	 * This call may generate runtime exceptions, when there is no space for the page table, or when there is not enough physical memory available.
	 * 
	 * This call is only used for the first part of the practical, without demand paging.
	 * 
	 * @param processId The identifier of the new process
	 * @param memorySize The size of the valid memory for this process
	 */
	public void reserveMemory (int processIdentifier, int memorySize);
	
	// read and write operations

	/**
	 * Writes a value "data" at the position given in the process address space. 
	 * 
	 * @param processIdentifier Identifier of the process
	 * @param virtualAdress Address in the process' virtual address space
	 * @param data Value to write
	 */
	public void write(int processIdentifier, int virtualAdress, int data);
	
	/**
	 * Reads a value at the position given in the process address space. 
	 * 
	 * @param processIdentifier Identifier of the process
	 * @param virtualAdress Address in the process' virtual address space
	 * @return the value at the given address
	 */
	public int read(int processIdentifier, int virtualAdress);
	
	// page table manipulation, used by the driver for part one and by the virtual memory for part two
	
	/**
	 * Get a complete page from the memory (e.g., to store it on disk).
	 * @param frameNumber
	 * @return
	 */
	public int[] getPage(int frameNumber);
	
	/**
	 * Replace a frame in physical memory with the provided one, the previous frame is overwritten.
	 * @param frameNumber
	 * @param replacementPage
	 */
	public void replacePage (int frameNumber, int[] replacementPage);
	
	/**
	 * Returns the TBL used by this MMU
	 * @return The TBL in use
	 */
	public TLB_interface getTLB ();
}
