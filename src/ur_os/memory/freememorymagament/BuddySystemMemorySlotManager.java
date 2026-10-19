/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os.memory.freememorymagament;

import java.util.ArrayList;
import java.util.HashMap;
import ur_os.memory.ProcessMemoryManager;
import ur_os.memory.contiguous.PMM_Contiguous;
import ur_os.memory.segmentation.PMM_Segmentation;
import ur_os.memory.segmentation.SegmentTableEntry;
import ur_os.process.Process;

/**
 *
 * @author super
 */
public class BuddySystemMemorySlotManager extends FreeMemorySlotManager {

    //base of the block -> size the process asked for
    private HashMap<Integer, Integer> requested;

    public BuddySystemMemorySlotManager(int memSize){
        super(memSize);
        requested = new HashMap();
        if(nextPowerOfTwo(memSize) != memSize){
            System.out.println("Warning - Buddy System needs a memory size that is a power of two");
        }
    }

    @Override
    public MemorySlot getSlot(int size) {

        if (size <= 0) {
            System.out.println("Error - Invalid memory request size: "+size);
            return null;
        }

        int blockSize = nextPowerOfTwo(size);

        //smallest free block that fits
        MemorySlot candidate = null;
        for (MemorySlot memorySlot : list) {
            if (memorySlot.getSize() >= blockSize) {
                if (candidate == null || memorySlot.getSize() < candidate.getSize()) {
                    candidate = memorySlot;
                }
            }
        }

        if (candidate == null) {
            System.out.println("Error - Memory cannot allocate a slot big enough for the requested memory");
            return null;
        }

        list.remove(candidate);

        int base = candidate.getBase();
        int currentSize = candidate.getSize();

        //split in halves, the upper half goes back to the free list
        while (currentSize > blockSize) {
            currentSize = currentSize / 2;
            MemorySlot buddy = new MemorySlot(base + currentSize, currentSize);
            addOrdered(buddy);
        }

        requested.put(base, size);

        return new MemorySlot(base, blockSize);
    }

    @Override
    public void reclaimMemory(Process p) {
        ProcessMemoryManager pmm = p.getPMM();
        switch (pmm.getType()) {
            case SEGMENTATION:
                PMM_Segmentation pmms = (PMM_Segmentation) p.getPMM();
                ArrayList<SegmentTableEntry> segments = pmms.getSt().getTable();
                for (SegmentTableEntry ste : segments) {
                    freeBlock(ste.getMemorySlot());
                }
                break;
            default:
            case CONTIGUOUS:
                PMM_Contiguous pmmc = (PMM_Contiguous) p.getPMM();
                freeBlock(pmmc.getMemorySlot());
                pmmc.setValid(false);
                break;
        }
    }

    //frees a block and joins it with its buddy while the buddy is free
    private void freeBlock(MemorySlot m) {
        if (m == null) {
            return;
        }
        int base = m.getBase();
        int size = nextPowerOfTwo(m.getSize());
        requested.remove(base);

        while (true) {
            int buddyBase = base ^ size;
            MemorySlot buddy = findFreeBlock(buddyBase, size);
            if (buddy == null) {
                break;
            }
            list.remove(buddy);
            base = Math.min(base, buddyBase);
            size = size * 2;
        }

        addOrdered(new MemorySlot(base, size));
    }

    @Override
    public int getInternalFragmentation() {
        int total = 0;
        for (Integer base : requested.keySet()) {
            total += nextPowerOfTwo(requested.get(base)) - requested.get(base);
        }
        return total;
    }

    //keeps the list ordered by base
    private void addOrdered(MemorySlot m) {
        int i = 0;
        while (i < list.size() && list.get(i).getBase() < m.getBase()) {
            i++;
        }
        list.add(i, m);
    }

    private MemorySlot findFreeBlock(int base, int size) {
        for (MemorySlot memorySlot : list) {
            if (memorySlot.getBase() == base && memorySlot.getSize() == size) {
                return memorySlot;
            }
        }
        return null;
    }

    private int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n) {
            p <<= 1;
        }
        return p;
    }

}
