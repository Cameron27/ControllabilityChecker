package net.sourceforge.waters.analysis.comp552;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;

import java.util.Arrays;
import java.util.HashMap;

public class StateTupleEncoder {
  private final HashMap<StateProxy, Integer>[] stateIndexMaps;
  private final int[] packMasks;
  private final int[] packSizes;
  private final StateProxy[][] indexStateMaps;

  public StateTupleEncoder(AutomatonProxy[] automata) {
    packMasks = new int[automata.length];
    packSizes = new int[automata.length];
    stateIndexMaps = new HashMap[automata.length];
    indexStateMaps = new StateProxy[automata.length][];

    // foreach automaton
    int i = 0;
    int sum = 0;
    for (AutomatonProxy automaton : automata) {
      // calculate the number of bits needed to uniquely represent all states
      packSizes[i] = getBitPackSize(automaton.getStates().size());
      sum += packSizes[i];
      packMasks[i] = packSizeToMask(packSizes[i]);

      stateIndexMaps[i] = new HashMap<>();
      indexStateMaps[i] = new StateProxy[automaton.getStates().size()];

      // create a mapping between all states and an index
      int j = 0;
      for (StateProxy state : automaton.getStates()) {
        stateIndexMaps[i].put(state, j);
        indexStateMaps[i][j] = state;
        j++;
      }

      i++;
    }

    // checks sum is under 64
    if (sum > 64) {
      throw new StateTupleSizeException("A tuple of states for these automata cannot be stored in 64 bits.");
    }
  }

  public long encode(StateProxy[] stateTuple) {
    if (stateTuple.length != packMasks.length)
      throw new IllegalArgumentException("State tuple does not contain the correct number of states.");

    long output = 0;

    for (int i = 0; i < stateTuple.length; i++) {
      StateProxy state = stateTuple[i];
      int stateIndex = stateIndexMaps[i].get(state);
      int mask = packMasks[i];
      int packSize = packSizes[i];

      output <<= packSize;
      output |= stateIndex & mask;
    }

    assert Arrays.deepEquals(stateTuple, decode(output));

    return output;
  }

  public StateProxy[] decode(long stateTupleCode) {
    StateProxy[] output = new StateProxy[packSizes.length];

    for (int i = output.length - 1; i >= 0; i--) {
      int stateIndex = (int) (stateTupleCode & packMasks[i]);
      stateTupleCode >>>= packSizes[i];

      output[i] = indexStateMaps[i][stateIndex];
    }

    return output;
  }

  private int getBitPackSize(int i) {
    return (byte) Math.ceil(Math.log(i) / Math.log(2));
  }

  private int packSizeToMask(int n) {
    int output = 0;
    for (int i = 0; i < n; i++) {
      output <<= 1;
      output |= 1;
    }

    return output;
  }

  public static class StateTupleSizeException extends RuntimeException {

    public StateTupleSizeException() {
    }

    public StateTupleSizeException(String message) {
      super(message);
    }

    public StateTupleSizeException(String message, Throwable cause) {
      super(message, cause);
    }

    public StateTupleSizeException(Throwable cause) {
      super(cause);
    }
  }
}
