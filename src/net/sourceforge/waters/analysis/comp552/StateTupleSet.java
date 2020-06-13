package net.sourceforge.waters.analysis.comp552;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * A set of state tuples, the previous state for all those states and a queue of unvisited states.
 */
public class StateTupleSet {
  /**
   * Map between all state tuples in the set and the previous state tuples for that state tuples.
   */
  private final Map<Long, Long> states;

  /**
   * Queue of unvisited state tuples.
   */
  private final Queue<Long> unvisitedStates;

  /**
   * Create an empty set of state tuples.
   */
  public StateTupleSet() {
    states = new HashMap();
    unvisitedStates = new ArrayDeque<>();
  }

  /**
   * Adds a state tuple to the sets and adds it to the queue of unvisited state tuples.
   *
   * @param state         The state to add.
   * @param previousState The previous state of the state to add.
   */
  public void add(long state, long previousState) {
    if (contains(state)) return;

    states.put(state, previousState);
    unvisitedStates.add(state);
  }

  /**
   * Returns true if there are unvisited state tuples.
   *
   * @return True if there are unvisited state tuples.
   */
  public boolean containsUnvisited() {
    return !unvisitedStates.isEmpty();
  }

  /**
   * Returns the first unvisited state tuple in the queue and removes it from the queue.
   *
   * @return The first unvisited state tuple in the queue.
   */
  public long removeUnvisited() {
    return unvisitedStates.remove();
  }

  /**
   * Returns true if a specific state tuple is in the set.
   *
   * @param state State tuple to check for.
   * @return True if state tuple is in the set.
   */
  public boolean contains(long state) {
    return states.containsKey(state);
  }

  /**
   * Gets the previous state tuple for a specified state tuple.
   *
   * @param currentStateCode The state tuple to get the previous state tuple of.
   * @return The previous state tuple.
   */
  public long getPrevious(long currentStateCode) {
    return states.get(currentStateCode);
  }
}
