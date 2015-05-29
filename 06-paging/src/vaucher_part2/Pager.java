package vaucher_part2;

import provided.Disk;
import provided.PagerInterface;

public class Pager implements PagerInterface {
	private Disk disk;
	/* register (counter) to store the next available frame */
	private int nextFreeFrame;
	/* total number of frames in the disk */
	private final int totalFrames;

	private int getFreePage() {
		// TODO: implement acquiring the next available disk frame
		// Throw an exception if the disk is full
		throw new RuntimeException("getFreePage: not implemented");
	}

	public int pageOut(int[] data, int diskFrame) {
		//TODO: implement paging-out
		// Store the received data array into the corresponding disk frame
		return diskFrame;
	}

	public int pageOut(int[] data) {
		return pageOut(data, getFreePage());
	}

	public int[] pageIn(int frame) {
		//TODO: implement page-in
		// Read from the disk the corresponding frame and return it
		return null;
	}

	public Pager(Disk disk) {
		//TODO: initialize pager
		// Calculate frames in disk
		// Set up next available frame in disk
		totalFrames = -1;
	}
}
