/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os.memory.freememorymagament;

/**
 *
 * @author super
 */
public class BestFitMemorySlotManager extends FreeMemorySlotManager{

    public BestFitMemorySlotManager(int memSize){
        super(memSize);
    }

    @Override
    public MemorySlot getSlot(int size) {
        MemorySlot m = null;
        //smallest slot where the request fits
        MemorySlot best = null;
        for (MemorySlot memorySlot : list) {
            if(memorySlot.canContain(size)){
                if(best == null || memorySlot.getSize() < best.getSize()){
                    best = memorySlot;
                }
            }
        }
        if(best == null){
            System.out.println("Error - Memory cannot allocate a slot big enough for the requested memory");
            return null;
        }
        if(best.getSize() == size){
            //exact size, the whole slot is used
            m = best;
            list.remove(m);
        }else{
            //take the first part of the slot, the rest stays free
            m = best.assignMemory(size);
        }
        return m;
    }

}
