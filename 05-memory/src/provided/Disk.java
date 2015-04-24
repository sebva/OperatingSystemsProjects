package provided;

/**
 * This class simulates a disk to be used for storing the frames that do not fit on memory.
 */
public class Disk {
	
	// constants and parameters for the simulation
	public static final int DISK_SIZE = 256 * 256 * 256 ; // 256*256 frames of 256 bytes each
	private static final int DISK_ACCESS_TIME = 10 ; // in milliseconds
	private static final int DISK_TRANSFER_TIME = 1 ; // in millisecond per byte
	
	// statistics
	private static int DISK_ACCESS_WRITE = 0;
	private static int DISK_ACCESS_READ = 0;
	private static int DISK_TRANSFER_READ = 0;
	private static int DISK_TRANSFER_WRITE = 0;
	
	// get statistics
	public static int getDiskIOTime () {
		return (DISK_ACCESS_READ+DISK_ACCESS_WRITE)*DISK_ACCESS_TIME + (DISK_TRANSFER_READ+DISK_TRANSFER_WRITE)*DISK_TRANSFER_TIME;
	}

	public static void resetDiskStats() {
		DISK_ACCESS_WRITE = 0;
		DISK_ACCESS_READ = 0;
		DISK_TRANSFER_READ = 0;
		DISK_TRANSFER_WRITE = 0;
	}
	
	// size of the disk
	private final int discSize ;

	// content of the disk
	private final int[] discContent ;

	private void checkAccessValid (int start, int length) {
		// check we are in the boundaries of the disk
		if (start >= discSize) {
			throw new RuntimeException("Trying to access disk of size "+discSize+" at offset "+start+": out of bounds");
		}
		if ((start + length) >= discSize) {
			throw new RuntimeException("Trying to access disk of size "+discSize+" at offset "+(start+length)+": out of bounds");
		}
	}

	public int[] getByteArray (int start, int length) {
		checkAccessValid(start, length);
		
		// prepare the return value
		int[] ret = new int[length];

		// copy the content into the ret array
		for (int i=start; i < (start + length); i++) {
			ret[i-start] = discContent[i];
		}	
		
		// update statistics
		DISK_ACCESS_READ++;
		DISK_TRANSFER_READ+=length;
		
		return ret;
	}

	public void setByteArray (int start, int length, int[] data) {
		checkAccessValid(start, length);
		
		// check the data provided is of the correct size
		if (data.length != length) {
			throw new RuntimeException("Trying to write "+length+" elements but provided an array of "+data.length+" elements.");
		}
		
		// copy the content
		for (int i=start; i < (start + length); i++) {
			discContent[i] = data[i-start];
		}
		
		// update statistics
		DISK_ACCESS_WRITE++;
		DISK_TRANSFER_WRITE+=length;
	}

	public Disk (int discSize) {
		this.discSize = discSize ;
		// create an array representing the content
		discContent = new int[discSize];
		// initialize the disc to zero
		for (int i = 0; i < discSize; i++) {
			discContent[i] = 0 ;
		}
	}
	
	public int getDiskSize() {
		return this.discSize;
	}
}
