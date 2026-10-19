/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os.memory.freememorymagament;

/**
 *
 * @author super
 */
public class WorstFitMemorySlotManager extends FreeMemorySlotManager{

    public WorstFitMemorySlotManager(int memSize){
        super(memSize);
    }

    @Override
    public MemorySlot getSlot(int size) {
        MemorySlot m = null;
        //largest slot in the list
        MemorySlot worst = null;
        for (MemorySlot memorySlot : list) {
            if(worst == null || memorySlot.getSize() > worst.getSize()){
                worst = memorySlot;
            }
        }
        //if it does not fit in the largest one, it does not fit anywhere
        if(worst == null || !worst.canContain(size)){
            System.out.println("Error - Memory cannot allocate a slot big enough for the requested memory");
            return null;
        }
        if(worst.getSize() == size){
            //exact size, the whole slot is used
            m = worst;
            list.remove(m);
        }else{
            //take the first part of the slot, the rest stays free
            m = worst.assignMemory(size);
        }
        return m;
    }

}
