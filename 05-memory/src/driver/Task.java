package driver;

import provided.MMU_Interface;
import vaucher_part1.MMU;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Task {

	// Task/process ID
	static private int pidCounter = 0;

	static synchronized int newPID() {
		return pidCounter++;
	}

	// Java is annoying if you want to use unsigned long values. Thus, we
	// strip the most significant bit in our memory value operations
	static private final long MASK = 0xFFFFFFF;

	static private int DEBUG_LEVEL = 0; // Add debugging messages

	private int pid; // Application ID
	private int programSize; // Application size
	private BufferedReader file; // Trace to be used
	private MMU_Interface memory; // Memory to be accessed

	private Map<Long, Long> memoryMap; // Used to ensure that values returned
	// from memory are correct
	private int lineCount; // Tracks in which line of the trace we are
	private boolean finished;

	/*
	 * A tasks encapsulates a memory trace from pintool, it is meant to be used
	 * to go through said trace and execute the memory references in it using
	 * the passed interface.
	 * 
	 * memory: memory interface to use traceFile: trace to follow size: the size
	 * of our application (all memory references will be converted to fit
	 * within)
	 */
	public Task(File traceFile, int size) throws FileNotFoundException {
		this.pid = newPID();
		this.file = new BufferedReader(new FileReader(traceFile));
		this.finished = false;
		this.programSize = size;
		this.lineCount = 0;
		
		memoryMap = new HashMap<Long, Long>((int)Math.ceil(size / 64.0));
		if (DEBUG_LEVEL > 0)
			System.out.println("New Task: " + traceFile.getName() + " Size: "
					+ size);
	}

	public void setMemory(MMU_Interface memory) {
		this.memory = memory;
		memory.reserveMemory(pid, programSize);
	}

	/*
	 * Executes the given number of instructions in the Task Trace Returns the
	 * amount of memory bytes referenced. Since every instruction is a 4-byte
	 * reference, it will normally return 4*instructions
	 */
	public int execute(int instructions) throws IOException {
		String str;
		String[] split;
		long address, offset;
		long value;
		int byteValue;
		int size;
		int bytesReferenced = 0;

		if (finished)
			return 0;

		while ((str = file.readLine()) != null && instructions-- > 0) {
			lineCount++;
			if (DEBUG_LEVEL > 1)
				System.out.println("" + lineCount + " Parsing[" + str + "]");

			if (str.startsWith("#")) // A comment
				continue;
			split = str.split("\\s+");
			if (split.length < 4)
				continue;

			// Because the address is down-sized to fit our application, a few
			// changes have to be applied to avoid reads/writes overlapping and
			// corrupting our expected results:
			// 1 - all address references are 4-byte aligned
			// 2 - all address references apply to 4-bytes

			address = Long.decode(split[2]); // Memory address to manipulate
			size = 4; // Integer.parseInt(split[3]); // Size of the fetch/write
			// instruction (1, 2, or 4)
			value = Long.decode(split[4]); // Value that was read/written from
			// memory

			address = address % programSize; // Limit it to our application's
			address -= address % 4; // Align it
			value = value & MASK; // Limit to lower seven significant bits
			offset = 0;

			if (DEBUG_LEVEL > 0)
				System.out.println(""+lineCount+" [" + str + "] -> [0x"
						+ Long.toHexString(address) + " 0x"
						+ Long.toHexString(value) + "]");

			// If the address was not previously written to, convert this read
			// to a write. Also do this in case of a mismatch (to ensure no
			// errors occurs from different addresses ending up hashed to the
			// same value.
			if (split[1].equals("W") || !memoryMap.containsKey(address)
					|| memoryMap.get(address).longValue() != value) {
				// Write
				memoryMap.put(address, value);
				while (offset < size) {
					byteValue = (int) (value & 0xFF);
					value = value >>> 8;
					memory.write(pid, (int) (address + offset), byteValue);
					if (DEBUG_LEVEL > 1)
						System.out.println("TW 0x" + Long.toHexString(address+offset) + ":" + byteValue);
					bytesReferenced++;
					offset++;
				}
			} else if (split[1].equals("R")) {
				// Read
				value = 0;
				Long prevValue = memoryMap.get(address);

				while (offset < size) {
					byteValue = memory.read(pid, (int) (address + offset));
					if (DEBUG_LEVEL > 1)
						System.out.println("TR 0x" + Long.toHexString(address+offset) + ":" + byteValue);
					value = (byteValue << (8 * offset)) | value;
					bytesReferenced++;
					offset++;
				}
				if (prevValue.longValue() != value) {
					throw new RuntimeException("Error at address 0x"
							+ Long.toHexString(address) + ": Memory read <0x"
							+ Long.toHexString(value)
							+ "> does not match last written value <0x"
							+ Long.toHexString(prevValue) + ">");
				}
			}
		}
		if (str == null)
			close();
		return bytesReferenced;
	}

	public void close() {
		finished = true;
		try {
			file.close();
		} catch (IOException e) {
			;
		}

	}

	public boolean isFinished() {
		return finished;
	}

	//UNIT TEST (assumes a trace in working directory)
	public static void main(String[] args) {
		Task t;
		try {
			t = new Task(new File("pinatrace.out"), 1 * 1024);
			t.setMemory(new MMU());
			t.execute(200000000);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
