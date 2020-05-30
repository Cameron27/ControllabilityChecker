package net.sourceforge.waters.analysis.comp552;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class StateTupleSet {
  private final Map<Long, Long> states;
  private final Queue<Long> unvisitedStates;

  public StateTupleSet() {
    states = new HashMap();
    unvisitedStates = new ArrayDeque<>();
  }

  public void add(long state, long previousState) {
    if (contains(state)) return;

    states.put(state, previousState);
    unvisitedStates.add(state);
  }

  public boolean containsUnvisited() {
    return unvisitedStates.peek() != null;
  }

  public long removeUnvisited() {
    return unvisitedStates.remove();
  }

  public boolean contains(long state) {
    return states.containsKey(state);
  }

  public long getPrevious(long currentStateCode) {
    return states.get(currentStateCode);
  }
}
