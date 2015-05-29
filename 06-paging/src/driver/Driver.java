package driver;

import provided.Disk;
import provided.MMU_Interface;
import provided.PhysicalMemory;
import provided.TLB_interface;

import javax.swing.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

/**
 * This class is the driver for the evaluation of your mechanisms.
 * 
 * For the first part of the exercise, it will call the MMU to create associations between a limited 
 * number of pages and frames, and issue reads and writes. These read and writes will lead to access
 * the physical memory, where the data and the page table are stored. You will be able to evaluate
 * the performance of your MMU when, at the end of the simulation, the total number of memory 
 * accesses and the time taken will be displayed.
 * 
 * Note that as explained in the subject we consider all operations but the access to memory and disk
 * as immediate.
 * 
 * For the second part, the driver will not populate the MMU's page table: the page table must be 
 * initialized such that, upon the first write to a legal page that was not created, a new empty page
 * is initialized and put in memory. A read to a non initialized page makes no sense, and will generate
 * an error (note that on a real system, you may just read a random value).
 * 
 * The workloads this driver generates can be tuned with the parameters below. Note that the workloads
 * generated mimic a workload with some locality of accesses.
 * 
 */
public class Driver {

	public enum MemoryType {
		MEMORY_UNSET, MEMORY_MMU, MEMORY_VM
	};

	private static MemoryType memoryType = MemoryType.MEMORY_UNSET;

	public static void setMemory(MemoryType type) {
		memoryType = type;

	}

	public static MMU_Interface getMemory() {
		switch (memoryType) {
		case MEMORY_MMU:
			//return new vaucher_part1.MMU();
		case MEMORY_VM:
			return new vaucher_part2.MMU();
		default:
			return null;
		}
	}

	public static void runSimulation(GUI gui, Vector<Task> tasks, int quantum) {

		// Exceptions thrown from a GUI-thread (eg: from code invoked by a click
		// event) are not easily caught/debugged with Eclipse, so we will catch
		// them here and print the relevant information to help debug.
		try {
			// Initialize the memory for the tasks and reset stats
			MMU_Interface memory = getMemory();
			PhysicalMemory.resetMemoryStats();
			Disk.resetDiskStats();
			
			int bytesReferenced = 0;
			if (memory == null) {
				gui.message("No memory model was specified. Was the correct class executed?");
				return;
			}
			
			for (Task t : tasks) {
				t.setMemory(memory);
			}
			gui.timeStamp();
			gui.message("Running simulation for " + tasks.size() + " tasks.");

			while (!tasks.isEmpty()) {
				Iterator<Task> it = tasks.iterator();
				while (it.hasNext()) {
					Task t = it.next();
					try {
						bytesReferenced += t.execute(quantum);
						if (t.isFinished()) {
							it.remove();
						}
					} catch (IOException e) {
						e.printStackTrace();
						gui
								.message("There was an I/O error during the simulation. Check the printed stack trace.\n");
						return;
					}
				}
			}
			gui.message("Simulation finished. Statistics:");
			gui.message("Total Bytes Referenced: " + bytesReferenced);
			gui.message("Memory Access Time: "
					+ PhysicalMemory.getTimeSpentAccessingMemory() + " ms");
			TLB_interface tlb = memory.getTLB();
			if (tlb != null)
				gui.message("TLB HitRate: " + tlb.getHitRate());
			else
				gui.message("No TLB associated.");
			gui.message("Disk Access Time: " + Disk.getDiskIOTime() + "ms");
			gui.message("Memory Reads: "+PhysicalMemory.getNbReads());
			gui.message("Memory Writes: "+PhysicalMemory.getNbWrites());
		} catch (RuntimeException e) {
			e.printStackTrace();
			gui
					.message("There was an error during the simulation. Check the printed stack trace.\n");
			gui.message("Message: " + e.getMessage());
			return;
		}
	}

	private static void launchGUI(int applicationCount) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUI i = new GUI(MMU_Interface.MAX_NUMBER_PROCESSES);
				i.pack();
				i.setVisible(true);
			}
		});
	}

	public static void generateMMUtest() {
		Driver.setMemory(MemoryType.MEMORY_MMU);
		launchGUI(MMU_Interface.MAX_NUMBER_PROCESSES);
	}

	public static void generateVMtest() {
		Driver.setMemory(MemoryType.MEMORY_VM);
		launchGUI(MMU_Interface.MAX_NUMBER_PROCESSES);
	}

	public static void main(String[] args) {
		generateVMtest();
	}
}
