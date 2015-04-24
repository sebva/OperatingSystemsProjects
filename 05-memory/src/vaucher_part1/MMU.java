package vaucher_part1;

import provided.MMU_Interface;
import provided.PagerInterface;
import provided.PhysicalMemory;
import provided.TLB_interface;

import java.util.HashMap;
import java.util.Map;

public class MMU implements MMU_Interface {

	/**
	 * The Page Table Base Register (PTBR) for each process
	 */
	private Map<Integer, Integer> pageTableBase;
	/**
	 * Similar register to obtain the Page Table Queue lists
	 */
	private Map<Integer, Integer> pageListBase;
	/**
	 * Register for storing the next free frame
	 */
	private int nextPhysicalFrame;

	/**
	 * Number of physical frames on memory
	 */
	private final int numberOfPhysicalFrames;

	/**
	 * These are used to store where in memory the next table goes (Page Table
	 * or Page Queue List)
	 */
	private int firstFreeEntryInPageTableArea;

	private int firstFreeEntryInPageListArea;

	private TLB_interface myTLB = null; //First Part
	private PagerInterface myPager = null; //Second Part

	public TLB_interface getTLB() {
		return myTLB;
	}
	
	/*
	 * Helping methods
	 */

	/* Binary flag methods: check if a flag is set, add a flag, remove a flag */
	private int setFlag(int v, int flag) {
		return v | flag;
	}

	private int removeFlag(int v, int flag) {
		return v & ~flag;
	}

	private boolean isFlagSet(int v, int flag) {
		return ((v & flag) != 0);
	}

	/*
	 * Byte manipulation methods: join two bytes to form a two-byte value, or
	 * split a value into its most/least significant bytes
	 * Example usage:
	 * int a = <big 32 bit number>
	 * int b = getMSB(a); //Get the upper 16 bit part
	 * int c = getLSB(a); //Get the lower 16 bit part
	 * assert(a == joinBytes(b,c)); //Joining them gives the original value
	 */
	private int joinBytes(int msb, int lsb) {
		return (msb << 8) | lsb;
	}

	private int getMSB(int value) {
		return (value & 0xff00) >>> 8;
	}

	private int getLSB(int value) {
		return value & 0xff;
	}

	/*
	 * Methods from the MMU_Interface
	 */
	private int findAndTakeFreeFrame(int processIdentifier) {
		//TODO: implement this method
		
		// Considerations (part 1)
		// Locate the next free frame and return it
		// When there are no more physical frames available, throw an exception
		throw new RuntimeException("No free memory frames available!");
		
		// Considerations (part 2)
		// Do not throw an exception, but select a frame (from processIdentifier) to page out
		// Use the process list's tail to know which page needs to be paged-out
		
		// During page-out, don't forget:
		// TLB associations need be removed
		// Old page's "page table" entry mode needs be updated
		// Page needs to be written to disk if necessary
		
	}

	public void reserveMemory(int processIdentifier, int memorySize) {
		//TODO: implement this method
		
		// Prerequisites
		// processIdentifier does not already exists
		// we don't have more than the max number of processes already
		
		// Considerations (part 1)
		// Locate where in memory the page table must go
		// Record this location in the pageTableBase register
		// Initialize the page table entries with either "used" for pages required, or "invalid" for the others
		// Used pages also need to reserve a physical memory frame
		
		// Considerations (part 2)
		// On-demand paging means that memory should not be reserved in this method
		// But all required entries are both "used" and "invalid" since they are not in memory
		// Locate where in memory the page list must go
		// Initialize the list's head/tail to zero
	}

	private int getPhysicalAdress(int processIdentifier, int virtualAddress,
			boolean forWriting) {

		//TODO: implement this method
		
		// Prerequisites
		// Check that processIdentifier is registered in our pageTableBase
		// Check that the virtualAddress falls within correct memory limits
		
		// Considerations (part one)
		// Calculate the page number and offset from the virtualAddress
		// If the frame-page association is not in the TLB, you must retrieve it from the page table 
		// If the page table's entry is marked as invalid, throw an exception (invalid memory accessed)
		// Update the TLB association

		// Considerations (part two)
		// If the page table's entry is marked as invalid AND used, the page is valid but not in memory: must page-in
		// Obtain a free Frame for this page
		// Add the page to the page list's head
		// If the page is marked as on disk, load it from disk and into memory
		// Update the page's mode (no longer invalid, also mark as dirty if used for a write)
		
		//Return the physical address for this virtual address
		return 0;
	}

	public int read(int processIdentifier, int virtualAddress) {
		// get the physical address for this process and virtual address
		int physicalAddress = getPhysicalAdress(processIdentifier,
				virtualAddress, false);
		// access physical memory
		return PhysicalMemory.readByte(physicalAddress);
	}

	public void write(int processIdentifier, int virtualAddress, int data) {
		// get the physical address for this process and virtual address
		int physicalAddress = getPhysicalAdress(processIdentifier,
				virtualAddress, true);
		// access physical memory
		PhysicalMemory.writeByte(physicalAddress, data);
	}

	// FOR THE SECOND PART OF THE ASSIGNMENT ONLY

	public int[] getPage(int frameNumber) {
		//TODO: implement this method
		// Just read the specified frame from memory and return it
		return null;
	}

	public void replacePage(int frameNumber, int[] replacementPage) {
		//TODO: implement this method
		// Just write into memory the replacement Page received
	}

	// CONSTRUCTOR

	public MMU() {
		//TODO: complete me
		pageTableBase = new HashMap<Integer, Integer>();
		pageListBase = new HashMap<Integer, Integer>();
		
		// Considerations
		// Calculate the memory size in frames
		numberOfPhysicalFrames = -1;
		
		// Calculate the memory required for the OS (in frames)
		// Reserve the OS space (set nextPhysicalFrame)
		
		//Calculate where in memory the first page table goes (set firstFreeEntryInPageTableArea)
		//Calculate where in memory the first page list goes  (set firstFreeEntryInPageListArea) 
		
		// create the TLB and Pager
	}

	// UNIT TEST
	public static void main(String[] args) {

		MMU m = new MMU();

		m.reserveMemory(2, 12800);

		System.out.println("nb reads: " + PhysicalMemory.getNbReads());
		System.out.println("nb writes:" + PhysicalMemory.getNbWrites());

		m.write(2, 250, 3);
		m.write(2, 510, 4);
		m.write(2, 760, 5);
		m.write(2, 1020, 6);
		m.write(2, 1200, 7);

		System.out.println("nb reads: " + PhysicalMemory.getNbReads());
		System.out.println("nb writes:" + PhysicalMemory.getNbWrites());

		// Or invoke the GUI
		//Driver.generateVMtest();
	}

}
