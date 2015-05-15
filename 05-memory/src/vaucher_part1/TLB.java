package vaucher_part1;

import provided.TLB_interface;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TLB implements TLB_interface {

    private int nbRequests = 0;
    private int nbSuccess = 0;
    /**
     * Map a process ID and a page number with the physical address
     */
    private final Map<TLBEntryIdentifier, Integer> tlbMap;
    private final Random random;
    private static final int TLB_SIZE = 16;

    public TLB() {
        tlbMap = new HashMap<TLBEntryIdentifier, Integer>(MMU.TLB_SIZE);
        random = new Random(97878967189L); // Predictable results
    }

    @Override
    public int getAssociation(int processIdentifier, int virtualPage) {
        nbRequests++;

        TLBEntryIdentifier key = new TLBEntryIdentifier(processIdentifier, virtualPage);
        int data = tlbMap.getOrDefault(key, -1);
        if(data != -1) {
            nbSuccess++;
        }
        return data;
    }

    @Override
    public void setAssociation(int processIdentifier, int virtualPage, int physicalFrame) {
        if(tlbMap.size() >= TLB_SIZE) {
            removeRandomAssociation();
        }
        tlbMap.put(new TLBEntryIdentifier(processIdentifier, virtualPage), physicalFrame);
    }

    @Override
    public void removeAssociation(int processIdentifier, int virtualPage) {
        tlbMap.remove(new TLBEntryIdentifier(processIdentifier, virtualPage));
    }

    @Override
    public float getHitRate() {
        return nbRequests == 0 ? 0 : nbSuccess / (float) nbRequests; // Prevent div. 0
    }

    private void removeRandomAssociation() {
        TLBEntryIdentifier[] keys = new TLBEntryIdentifier[tlbMap.size()];
        tlbMap.keySet().toArray(keys);
        tlbMap.remove(keys[random.nextInt(keys.length)]);
    }

    private static class TLBEntryIdentifier {
        public int processId;
        public int pageNumber;

        public TLBEntryIdentifier(int processId, int pageNumber) {
            this.processId = processId;
            this.pageNumber = pageNumber;
        }

        @Override
        public int hashCode() {
            return processId * 31 + pageNumber;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj != null && obj instanceof TLBEntryIdentifier) {
                TLBEntryIdentifier tlb = (TLBEntryIdentifier)obj;
                return tlb.processId == processId && tlb.pageNumber == pageNumber;
            }
            return false;
        }
    }
}
