package vaucher_part2;

import provided.Disk;
import provided.MMU_Interface;
import provided.PagerInterface;

import java.util.Arrays;

public class Pager implements PagerInterface {
	private Disk disk;
	/* register (counter) to store the next available frame */
	private int nextFreeFrame;
	/* total number of frames in the disk */
	private final int totalFrames;

	public Pager(Disk disk) {
        this.disk = disk;
		// Calculate frames in disk
		totalFrames = disk.getDiskSize() / MMU_Interface.PAGE_SIZE;
		// Set up next available frame in disk
		nextFreeFrame = 0;
	}

	private int getFreePage() {
		// Throw an exception if the disk is full
		if (nextFreeFrame >= totalFrames) {
            throw new RuntimeException("The disk is full");
        }

        return nextFreeFrame ++;
	}

	public int pageOut(int[] data, int diskFrame) {
		// Store the received data array into the corresponding disk frame
        disk.setByteArray(diskFrame, MMU_Interface.PAGE_SIZE, data);
		return diskFrame;
	}

	public int pageOut(int[] data) {
		return pageOut(data, getFreePage());
	}

	public int[] pageIn(int diskFrame) {
		// Read from the disk the corresponding frame and return it
        return disk.getByteArray(diskFrame, MMU_Interface.PAGE_SIZE);
	}

    public static void main(String[] args) {
        Pager pager = new Pager(new Disk(10000));
        int[] tab = new int[256];
        for (int i = 0; i < 256; i++) {
            tab[i] = i;
        }
        pager.pageOut(tab);
        int adr = pager.pageOut(tab);
        int[] data = pager.pageIn(adr);
        if(!Arrays.equals(tab, data)) {
            System.err.println("Pager broken");
        }
    }
}
