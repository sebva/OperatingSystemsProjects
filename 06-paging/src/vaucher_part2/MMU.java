package vaucher_part2;

import provided.*;

import java.util.HashMap;
import java.util.Map;

public class MMU implements MMU_Interface {

    /**
     * Number of physical frames on memory
     */
    private final int numberOfPhysicalFrames;
    /**
     * The Page Table Base Register (PTBR) for each process
     */
    private final Map<Integer, Integer> pageTableBase;
    /**
     * Similar register to obtain the Page Table Queue lists
     */
    private final Map<Integer, Integer> pageListBase;
    /**
     * Register for storing the next free frame
     */
    private int nextPhysicalFrame;
    /**
     * These are used to store where in memory the next table goes (Page Table
     * or Page Queue List)
     */
    private int firstFreeEntryInPageTableArea;
    private int firstFreeEntryInPageListArea;

    private final TLB_interface myTLB; //First Part
    private final PagerInterface myPager; //Second Part

    // CONSTRUCTOR
    public MMU() {
        pageTableBase = new HashMap<>();
        pageListBase = new HashMap<>();

        // Considerations
        // Calculate the memory size in frames
        numberOfPhysicalFrames = PhysicalMemory.MEMORY_SIZE / PAGE_SIZE;

        // Calculate the memory required for the OS (in frames)
        int pageTablesSpace = NB_PAGES_IN_PROCESS_ADDRESS_SPACE * 4 * MAX_NUMBER_PROCESSES;
        int queuesSpace = (NB_PAGES_IN_PROCESS_ADDRESS_SPACE + 2) * MAX_NUMBER_PROCESSES;
        // Reserve the OS space (set nextPhysicalFrame)
        nextPhysicalFrame = (int) (Math.ceil((pageTablesSpace + queuesSpace) / (double)PAGE_SIZE));

        //Calculate where in memory the first page table goes (set firstFreeEntryInPageTableArea)
        firstFreeEntryInPageTableArea = 0;
        //Calculate where in memory the first page list goes  (set firstFreeEntryInPageListArea)
        firstFreeEntryInPageListArea = pageTablesSpace + 1;

        // create the TLB and Pager
        myTLB = new TLB();
        myPager = new Pager(new Disk(Disk.DISK_SIZE));
    }

    // UNIT TEST
    public static void main(String[] args) {

        MMU m = new MMU();

        m.reserveMemory(1, 65536);
        m.reserveMemory(2, 65536);
        for (int i = 0; i < 65536; i++) {
            m.write(1, i, 1);
            m.write(2, i, 2);
        }

        for (int i = 0; i < 65536; i++) {
            int a = m.read(2, i);
            m.write(1, i, 3);
            int b = m.read(1, i);
        }
        // Or invoke the GUI
        //Driver.generateVMtest();
    }

    @Override
    public TLB_interface getTLB() {
        return myTLB;
    }

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

    private int findAndTakeFreeFrame(int processIdentifier) {
        // Considerations (part 2)
        // Do not throw an exception, but select a frame (from processIdentifier) to page out
        // Use the process list's tail to know which page needs to be paged-out
        if(nextPhysicalFrame >= numberOfPhysicalFrames) {
            int pageListLocation = pageListBase.get(processIdentifier);
            int tail = PhysicalMemory.readByte(pageListLocation + 1);
            int frameId = PhysicalMemory.readByte(pageListLocation + 2 + tail);
            int pageTableLocation = pageTableBase.get(processIdentifier) + frameId * 4;

            int frame = PhysicalMemory.readByte(pageTableLocation + 1);
            int flags = PhysicalMemory.readByte(pageTableLocation);
            int[] page = getPage(frame);

            if (isFlagSet(flags, FLAG_ON_DISK)) {
                int diskFrameMsb = PhysicalMemory.readByte(pageTableLocation + 2);
                int diskFrameLsb = PhysicalMemory.readByte(pageTableLocation + 3);
                int diskFrame = joinBytes(diskFrameMsb, diskFrameLsb);
                myPager.pageOut(page, diskFrame);
            }
            else {
                int diskFrame = myPager.pageOut(page);
                PhysicalMemory.writeByte(pageTableLocation + 2, getMSB(diskFrame));
                PhysicalMemory.writeByte(pageTableLocation + 3, getLSB(diskFrame));
                // Update flag
                flags = setFlag(flags, FLAG_ON_DISK);
            }

            flags = setFlag(flags, FLAG_INVALID);
            PhysicalMemory.writeByte(pageTableLocation, flags);

            // Increment tail counter
            PhysicalMemory.writeByte(pageListLocation + 1, (tail + 1) % PAGE_SIZE);

            return frame;
        }
        else {
            return nextPhysicalFrame++;
        }


        // During page-out, don't forget:
        // TLB associations need be removed
        // Old page's "page table" entry mode needs be updated
        // Page needs to be written to disk if necessary

    }

    @Override
    public void reserveMemory(int processIdentifier, int memorySize) {
        // Prerequisites
        // processIdentifier does not already exists
        if(pageTableBase.containsKey(processIdentifier)) {
            throw new RuntimeException("processIdentifier already exists");
        }
        // we don't have more than the max number of processes already
        if(pageTableBase.size() >= MAX_NUMBER_PROCESSES) {
            throw new RuntimeException("Max number of processes already reached");
        }
        if(memorySize > PhysicalMemory.MEMORY_SIZE) {
            throw new RuntimeException("Trying to reserve too much memory");
        }

        // Locate where in memory the page table must go
        int pageTableLocation = firstFreeEntryInPageTableArea;
        // Record this location in the pageTableBase register
        pageTableBase.put(processIdentifier, pageTableLocation);
        firstFreeEntryInPageTableArea += numberOfPhysicalFrames * 4;

        // Locate where in memory the page list must go
        int pageListLocation = firstFreeEntryInPageListArea;
        pageListBase.put(processIdentifier, pageListLocation);
        firstFreeEntryInPageListArea += numberOfPhysicalFrames + 2;

        // On-demand paging means that memory should not be reserved in this method
        // But all required entries are both "used" and "invalid" since they are not in memory
        for (int i = 0; i < NB_PAGES_IN_PROCESS_ADDRESS_SPACE; i++) {
            int flags = FLAG_INVALID;
            flags = setFlag(flags, FLAG_USED);

            int pageAddress = pageTableLocation + i * 4;
            PhysicalMemory.writeByte(pageAddress, flags);
            PhysicalMemory.writeByte(pageAddress + 1, 0);
            PhysicalMemory.writeByte(pageAddress + 2, 0);
            PhysicalMemory.writeByte(pageAddress + 3, 0);
        }
        // Used pages also need to reserve a physical memory frame

        // Considerations (part 2)


        // Initialize the list's head/tail to zero
        PhysicalMemory.writeByte(pageListLocation, 0);
        PhysicalMemory.writeByte(pageListLocation + 1, 0);
    }

    private int getPhysicalAddress(int processIdentifier, int virtualAddress, boolean forWriting) {
        // Prerequisites
        // Check that processIdentifier is registered in our pageTableBase
        if(!pageTableBase.containsKey(processIdentifier)) {
            throw new RuntimeException("Process identifier not registered");
        }
        // Check that the virtualAddress falls within correct memory limits
        if(virtualAddress < 0 || virtualAddress >= PhysicalMemory.MEMORY_SIZE) {
            throw new RuntimeException("Virtual address outside of bounds");
        }

        // Considerations (part one)
        // Calculate the page number and offset from the virtualAddress
        int pageNumber = virtualAddress / PAGE_SIZE;
        int frame;

        // Considerations (part two)
        int pageTableLocation = pageTableBase.get(processIdentifier) + pageNumber * 4;
        int pageListLocation = pageListBase.get(processIdentifier);
        int flags = PhysicalMemory.readByte(pageTableLocation);

        // If the page table's entry is marked as invalid AND used, the page is valid but not in memory: must page-in
        if (isFlagSet(flags, FLAG_INVALID)) {
            if (isFlagSet(flags, FLAG_USED)) {
                // Obtain a free Frame for this page
                frame = findAndTakeFreeFrame(processIdentifier);
                PhysicalMemory.writeByte(pageTableLocation + 1, frame);

                if (isFlagSet(flags, FLAG_ON_DISK)) {
                    // If the page is marked as on disk, load it from disk and into memory
                    // Update the page's mode (no longer invalid, also mark as dirty if used for a write)
                    int diskFrameMsb = PhysicalMemory.readByte(pageTableLocation + 2);
                    int diskFrameLsb = PhysicalMemory.readByte(pageTableLocation + 3);
                    int diskFrame = joinBytes(diskFrameMsb, diskFrameLsb);

                    replacePage(frame, myPager.pageIn(diskFrame));
                }

                flags = removeFlag(flags, FLAG_INVALID);
                if (forWriting) {
                    flags = setFlag(flags, FLAG_DIRTY);
                }
                // Add the page to the page list's head
                int head = PhysicalMemory.readByte(pageListLocation);
                PhysicalMemory.writeByte(head + pageListLocation + 2, pageNumber);
                PhysicalMemory.writeByte(pageListLocation, (head + 1) % PAGE_SIZE);
            } else {
                throw new RuntimeException("Invalid page");
            }
        }
        else {
            frame = PhysicalMemory.readByte(pageTableLocation + 1);
        }

        PhysicalMemory.writeByte(pageTableLocation, flags);
        // If the frame-page association is not in the TLB, you must retrieve it from the page table
        /* Part 1 TLB Stuff
        frame = myTLB.getAssociation(processIdentifier, virtualAddress);
        if(frame == -1) {
            int pageTableLocation = pageTableBase.get(processIdentifier) + pageNumber * 2;
            int flags = PhysicalMemory.readByte(pageTableLocation);
            // If the page table's entry is marked as invalid, throw an exception (invalid memory accessed)
            if (isFlagSet(flags, FLAG_INVALID)) {
                throw new RuntimeException("Trying to access an invalid frame");
            }
            frame = PhysicalMemory.readByte(pageTableLocation + 1);

            // Update the TLB association
            myTLB.setAssociation(processIdentifier, virtualAddress, frame);
        }
        //*/


        //Return the physical address for this virtual address
        int offset = virtualAddress % PAGE_SIZE;
        return frame * PAGE_SIZE + offset;
    }

    @Override
    public int read(int processIdentifier, int virtualAddress) {
        // get the physical address for this process and virtual address
        int physicalAddress = getPhysicalAddress(processIdentifier,
                virtualAddress, false);
        // access physical memory
        return PhysicalMemory.readByte(physicalAddress);
    }

    @Override
    public void write(int processIdentifier, int virtualAddress, int data) {
        // get the physical address for this process and virtual address
        int physicalAddress = getPhysicalAddress(processIdentifier,
                virtualAddress, true);
        // access physical memory
        PhysicalMemory.writeByte(physicalAddress, data);
    }

    // FOR THE SECOND PART OF THE ASSIGNMENT ONLY

    @Override
    public int[] getPage(int frameNumber) {
        // Just read the specified frame from memory and return it
        int[] data = new int[PAGE_SIZE];
        for (int i = 0; i < PAGE_SIZE; i++) {
            data[i] = PhysicalMemory.readByte(frameNumber + i);
        }
        return data;
    }

    @Override
    public void replacePage(int frameNumber, int[] replacementPage) {
        // Just write into memory the replacement Page received
        for (int i = 0; i < replacementPage.length; i++) {
            PhysicalMemory.writeByte(frameNumber + i, replacementPage[i]);
        }
    }

}
