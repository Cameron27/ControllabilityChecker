package net.sourceforge.waters.analysis.comp552;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class StateTupleSet {
  private final Set<Long> states;
  private final Queue<Long> unvisitedStates;

  public StateTupleSet() {
    states = new TreeSet<>();
    unvisitedStates = new ArrayDeque<>();
  }

  public void add(long state) {
    if (contains(state)) return;

    states.add(state);
    unvisitedStates.add(state);
  }

  public boolean containsUnvisited() {
    return unvisitedStates.peek() != null;
  }

  public long removeUnvisited() {
    return unvisitedStates.remove();
  }

  public boolean contains(long state) {
    return states.contains(state);
  }
}
