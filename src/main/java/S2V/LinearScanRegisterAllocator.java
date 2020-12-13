package S2V;

import Typecheck.Pair;
import cs132.IR.registers.Registers;

import java.util.*;

public class LinearScanRegisterAllocator {
    public Map<String, Map<String, String>> registerAllocations;
    public Map<String, Pair<Integer, Integer>> active;
    private Map<String, Map<String, Pair<Integer, Integer>>> liveness;
    private Map<String, Pair<Integer, Integer>> currentLiveSet;
    private Set<String> freeRegisters;
    int numRegs;

    public LinearScanRegisterAllocator(Map<String, Map<String, Pair<Integer, Integer>>> liveness) {
        registerAllocations = new HashMap<>();
        active = new HashMap<>();
        this.liveness = liveness;
        numRegs = Registers.riscVregs.size() - 8;

        for (String funcName : liveness.keySet()) {
            freeRegisters = new HashSet<>(Registers.riscVregs);
            freeRegisters.remove("s10");
            freeRegisters.remove("s11");
            freeRegisters.remove("a2");
            freeRegisters.remove("a3");
            freeRegisters.remove("a4");
            freeRegisters.remove("a5");
            freeRegisters.remove("a6");
            freeRegisters.remove("a7");

            linearScanRegisterAllocation(funcName);
        }
    }

    private void linearScanRegisterAllocation(String funcName) {
        // Setup
        Map<String, Pair<Integer, Integer>> currentLiveSet = liveness.get(funcName);

        ArrayList<Map.Entry<String, Pair<Integer, Integer>>> sortedLiveSet = new ArrayList<>();

        for (Map.Entry<String, Pair<Integer, Integer>> varEntry : currentLiveSet.entrySet()) {
            sortedLiveSet.add(varEntry);
        }

        sortedLiveSet.sort(Comparator.comparingInt(o -> o.getValue().fst));

        registerAllocations.put(funcName, new HashMap<>());

        active = new HashMap<>();
        int paramCount = 2;
        for (Map.Entry<String, Pair<Integer, Integer>> varEntry : sortedLiveSet) {
            if (varEntry.getValue().fst < 0) {
                if (paramCount <= 7) {
                    registerAllocations.get(funcName).put(varEntry.getKey(), "a"+paramCount);
                    paramCount++;
                }
                continue;
            }

            expireOldIntervals(varEntry.getValue(), funcName);
            if (active.size() == numRegs) {
                spillAtInterval(varEntry, funcName);
            } else {
                for (String nextReg : freeRegisters) {
                    registerAllocations.get(funcName).put(varEntry.getKey(), nextReg);
                    freeRegisters.remove(nextReg);
                    break;
                }
                active.put(varEntry.getKey(), varEntry.getValue());
            }
        }
    }

    private void expireOldIntervals(Pair<Integer, Integer> i, String funcName) {
        // Setup
        ArrayList<Map.Entry<String, Pair<Integer, Integer>>> sortedActive = new ArrayList<>();

        for (Map.Entry<String, Pair<Integer, Integer>> varEntry : active.entrySet()) {
            sortedActive.add(varEntry);
        }

        sortedActive.sort(new Comparator<Map.Entry<String, Pair<Integer, Integer>>>() {
            @Override
            public int compare(Map.Entry<String, Pair<Integer, Integer>> o1, Map.Entry<String, Pair<Integer, Integer>> o2) {
                return o2.getValue().snd - o1.getValue().snd;
            }
        });

        for (Map.Entry<String, Pair<Integer, Integer>> varEntry : sortedActive){
            if (varEntry.getValue().snd >= i.fst) {
                return;
            }
            active.remove(varEntry.getKey());
            freeRegisters.add(registerAllocations.get(funcName).get(varEntry.getKey()));
        }
    }

    private void spillAtInterval(Map.Entry<String, Pair<Integer, Integer>> i, String funcName) {
        // Setup
        ArrayList<Map.Entry<String, Pair<Integer, Integer>>> sortedActive = new ArrayList<>();

        for (Map.Entry<String, Pair<Integer, Integer>> varEntry : active.entrySet()) {
            sortedActive.add(varEntry);
        }

        sortedActive.sort(new Comparator<Map.Entry<String, Pair<Integer, Integer>>>() {
            @Override
            public int compare(Map.Entry<String, Pair<Integer, Integer>> o1, Map.Entry<String, Pair<Integer, Integer>> o2) {
                return o2.getValue().snd - o1.getValue().snd;
            }
        });

        Map.Entry<String, Pair<Integer, Integer>> spill = sortedActive.get(0);
        if (spill.getValue().snd > i.getValue().snd) {
            registerAllocations.get(funcName).put(i.getKey(), registerAllocations.get(funcName).remove(spill.getKey()));
            active.remove(spill.getKey());
            active.put(i.getKey(), i.getValue());
        }
    }
}
