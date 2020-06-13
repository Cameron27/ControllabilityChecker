package net.sourceforge.waters.analysis.comp552;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Used to convert between state tuples and state tuple codes.
 */
public class StateTupleEncoder {
  /**
   * Array containing the hashmaps mapping between states and that states index in indexStateMaps for each automata.
   */
  private final HashMap<StateProxy, Integer>[] stateIndexMaps;

  /**
   * Array of masks for each automata.
   */
  private final int[] packMasks;

  /**
   * Array of number rof bits needed to represent the state for each automata.
   */
  private final int[] packSizes;

  /**
   * Array of arrays of states in each automata.
   */
  private final StateProxy[][] indexStateMaps;

  /**
   * Construct an encoder by calculating the number of bits that are needed to represent a state for each automata in
   * the model.
   *
   * @param automata List of automata in the model that this encoder will be for.
   */
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
      packMasks[i] = createMask(packSizes[i]);

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
      throw new StateTupleSizeException(String.format("A tuple of states for these automata cannot be stored in 64 bits, %d bits would be needed.", sum));
    }
  }

  /**
   * Convert a state tuple to a state tuple code.
   *
   * @param stateTuple The state tuple to convert.
   * @return The state tuple code representing the state tuple.
   */
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

  /**
   * Convert a state tuple code to an array of states.
   *
   * @param stateTupleCode The state tuple code to convert.
   * @return The state tuple the stat tuple code represents.
   */
  public StateProxy[] decode(long stateTupleCode) {
    StateProxy[] output = new StateProxy[packSizes.length];

    for (int i = output.length - 1; i >= 0; i--) {
      int stateIndex = (int) (stateTupleCode & packMasks[i]);
      stateTupleCode >>>= packSizes[i];

      output[i] = indexStateMaps[i][stateIndex];
    }

    return output;
  }

  /**
   * Calculates the number of bits needed to represent a given number of states.
   *
   * @param n The number of states that need to be represented.
   * @return The number of bits needed.
   */
  private int getBitPackSize(int n) {
    return (byte) Math.ceil(Math.log(n) / Math.log(2));
  }

  /**
   * Creates a bit mask of a given size.
   *
   * @param n The size of the bit mask.
   * @return The bit mask.
   */
  private int createMask(int n) {
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
