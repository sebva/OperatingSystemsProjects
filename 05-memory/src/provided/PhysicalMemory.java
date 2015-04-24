package provided;

public class PhysicalMemory {
	public static final int MEMORY_SIZE = 256 * 256 ; // 256 frames of 256 bytes each
	
	// constants and parameters for the simulation
	private static final double MEMORY_ACCESS_TIME = 0.000100 ; // in milliseconds

	// content of the memory
	private static final int[] memoryContent = new int[MEMORY_SIZE];

	private static int nbReads = 0;
	private static int nbWrites = 0;
	
	public static int getNbReads () { return nbReads; }
	public static int getNbWrites () { return nbWrites; }
	public static double getTimeSpentAccessingMemory () { return (nbReads+nbWrites)*MEMORY_ACCESS_TIME; }
	public static void resetMemoryStats() {
		nbReads = 0;
		nbWrites = 0;
	}
	
	public static void writeByte (int position, int value) {
		// check that the written value would fit in a byte 
		if (value < 0 || value > 255) {
			System.err.println("Warning: trying to write value "+value+" at position "+position+" in physical memory ; not supported by the byte");
		}
		if (position < 0 || position >= MEMORY_SIZE) {
			throw new RuntimeException("Error, trying to write outside physical memory bounds (position: "+position+")");
		}
		memoryContent[position] = value ;
		// account for a write
		nbWrites++;
	}
	
	public static int readByte (int position) {
		if (position < 0 || position >= MEMORY_SIZE) {
			throw new RuntimeException("Error, trying to read outside physical memory bounds (position: "+position+")");
		}
		// accounts for a read
		nbReads++;
		return memoryContent[position];
	}
	
	public PhysicalMemory (int memorySize) {
		throw new RuntimeException("The PhysicalMemory class should not be instantiated");
	}
}
