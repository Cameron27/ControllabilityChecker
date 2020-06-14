//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.comp552;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.*;

import java.util.*;

/**
 * <P>A dummy implementation of a controllability checker.</P>
 *
 * <P>The {@link #run()} method of this model checker does nothing,
 * and simply claims that every model is controllable.</P>
 *
 * <P>You are welcome to edit this file as much as you like,
 * but please <STRONG>do not change</STRONG> the public interface.
 * Do not change the signature of the constructor,
 * or of the {@link #run()} or {@link #getCounterExample()} methods.
 * You should expect a single constructor call, followed by several calls
 * to {@link #run()} and {@link #getCounterExample()}, so your code needs
 * to be reentrant.</P>
 *
 * <P><STRONG>WARNING:</STRONG> If you do not comply with these rules, the
 * automatic tester may fail to run your program, resulting in 0 marks for
 * your assignment.</P>
 *
 * @author Robi Malik
 * @see ModelChecker
 */
public class ControllabilityChecker extends ModelChecker {
  //#########################################################################
  //# Data Members
  /**
   * The computed counterexample or null if the model is controllable.
   */
  private SafetyCounterExampleProxy mCounterExample;

  /**
   * Array of all the automata in the model.
   */
  private AutomatonProxy[] automata;

  /**
   * Array of all events in the model.
   */
  private EventProxy[] events;

  /**
   * An array of the sets of event explicitly defined in the various automata in the model.
   */
  private Set<EventProxy>[] eventsInAutomata;

  /**
   * An array of mappings between source states and transitions states for each automaton.
   */
  private Map<StateProxy, List<TransitionProxy>>[] transitionsBySource;

  /**
   * A state tuple encoder to convert between a state tuple and the long representation of the state tuple.
   */
  private StateTupleEncoder stateTupleEncoder;

  /**
   * Set to contain all encountered state.
   */
  private StateTupleSet stateTupleSet;

  //#########################################################################
  //# Constructors

  /**
   * Creates a new controllability checker to check a particular model.
   *
   * @param model      The model to be checked by this controllability
   *                   checker.
   * @param desFactory Factory used for trace construction.
   */
  public ControllabilityChecker(final ProductDESProxy model,
                                final ProductDESProxyFactory desFactory) {
    super(model, desFactory);
  }

  //#########################################################################
  //# Invocation

  /**
   * Runs this controllability checker.
   * This method starts the model checking process on the model given
   * as parameter to the constructor of this object. On termination,
   * if the result is false, a counterexample can be queried using the
   * {@link #getCounterExample()} method.
   *
   * @return <CODE>true</CODE> if the model is controllable, or
   * <CODE>false</CODE> if it is not.
   */
  @Override
  public boolean run() {
    setup();

    // create state set and add initial state
    stateTupleSet = new StateTupleSet();
    long initialStateCode = stateTupleEncoder.encode(getInitialState());
    stateTupleSet.add(initialStateCode, initialStateCode);

    StateProxy[] nextState = new StateProxy[automata.length];
    // continue while there are unvisited states
    while (stateTupleSet.containsUnexpanded()) {
      long currentStateCode = stateTupleSet.popUnexpanded();
      StateProxy[] currentState = stateTupleEncoder.decode(currentStateCode);

      // check every event for a legal transition
      for (EventProxy event : events) {
        StateProxy temp;

        boolean success = true;
        // check the event for every state in the state tuple
        for (int i = 0; i < nextState.length; i++) {
          AutomatonProxy automaton = automata[i];
          // check for implicit self loop
          if (!eventsInAutomata[i].contains(event)) {
            nextState[i] = currentState[i];
          }
          // check for explicit transition
          else if ((temp = getTargetFromSourceAndEvent(i, currentState[i], event)) != null) {
            nextState[i] = temp;
          }
          // if no transition was found, the event is uncontrollable and the current state being checked is from a spec,
          // fail and compute counter example
          else if (event.getKind() == EventKind.UNCONTROLLABLE && automaton.getKind() == ComponentKind.SPEC) {
            mCounterExample = computeCounterExample(currentState, event);
            assert isCounterExample(mCounterExample);
            return false;
          }
          // the event does not work so stop
          else {
            success = false;
            break;
          }
        }

        // add new state if event worked
        if (success) {
          stateTupleSet.add(stateTupleEncoder.encode(nextState), currentStateCode);
        }
      }
    }

    return true;
  }

  /**
   * Given a source state and event, finds the target state if a transition exists for that source and event. Dose not
   * check for implicit self loops.
   *
   * @param automatonIndex Index of the automaton the source state is in.
   * @param source         The source state.
   * @param event          The event event to check for.
   * @return The target state of the transition containing the source state and event, null if no such transition was found.
   */
  private StateProxy getTargetFromSourceAndEvent(int automatonIndex, StateProxy source, EventProxy event) {
    // get list of transitions for the source
    List<TransitionProxy> possibleTransitions = transitionsBySource[automatonIndex].get(source);

    // check if any transitions exist, if not return null
    if (possibleTransitions == null) return null;

    // check every transition for the specified event
    for (TransitionProxy possibleTransition : possibleTransitions) {
      if (possibleTransition.getEvent() == event) return possibleTransition.getTarget();
    }

    // if nothing was found, return null
    return null;
  }

  /**
   * Gets the initial state tuple.
   *
   * @return The initial state tuple.
   */
  private StateProxy[] getInitialState() {
    StateProxy[] initialState = new StateProxy[automata.length];

    // for each automaton
    for (int i = 0; i < automata.length; i++) {
      AutomatonProxy automaton = automata[i];

      // find the initial state
      for (StateProxy state : automaton.getStates()) {
        if (state.isInitial()) {
          initialState[i] = state;
          break;
        }
      }
    }

    return initialState;
  }

  /**
   * Setup all the data structures needed.
   */
  private void setup() {
    automata = getAutomata();
    events = getEvents();
    eventsInAutomata = getEventsInAutomata();
    transitionsBySource = getTransitionsBySource();
    stateTupleEncoder = new StateTupleEncoder(automata);
  }

  /**
   * Creates an array of all the automata in the model with the plant models at the start and the specifications at
   * the end.
   *
   * @return An array of all the automata in the model.
   */
  private AutomatonProxy[] getAutomata() {
    LinkedList<AutomatonProxy> automata = new LinkedList<>();

    // add each automaton to start or end of list
    for (AutomatonProxy automaton : getModel().getAutomata()) {
      switch (automaton.getKind()) {
        case PLANT:
          automata.addFirst(automaton);
          break;
        case SPEC:
          automata.addLast(automaton);
          break;
        default:
          break;
      }
    }
    return automata.toArray(new AutomatonProxy[0]);
  }

  /**
   * Creates an array of all the events in the model with the uncontrollable events at the start and the controllable
   * events at the end.
   *
   * @return An array of all the events in the model.
   */
  private EventProxy[] getEvents() {
    LinkedList<EventProxy> events = new LinkedList<>();

    // add each event to start or end of list
    for (EventProxy event : getModel().getEvents()) {
      switch (event.getKind()) {
        case UNCONTROLLABLE:
          events.addFirst(event);
          break;
        case CONTROLLABLE:
          events.addLast(event);
      }
    }
    return events.toArray(new EventProxy[0]);
  }

  /**
   * Creates sets of all the events explicitly defined in each automaton.
   *
   * @return An array with the sets of events explicitly defined in each automaton.
   */
  private Set<EventProxy>[] getEventsInAutomata() {
    Set<EventProxy>[] eventsInAutomata = new HashSet[automata.length];

    // create a set of events for each automaton
    for (int i = 0; i < automata.length; i++) {
      AutomatonProxy automaton = automata[i];
      eventsInAutomata[i] = new HashSet<>();
      for (EventProxy event : automaton.getEvents()) {
        if (event.getKind() == EventKind.PROPOSITION) continue;
        eventsInAutomata[i].add(event);
      }
    }

    return eventsInAutomata;
  }

  /**
   * Creates a mapping between states and all the transitions with that state as a source for each automaton.
   *
   * @return An array with the mappings between states and transitions with that state as a source.
   */
  private Map<StateProxy, List<TransitionProxy>>[] getTransitionsBySource() {
    Map<StateProxy, List<TransitionProxy>>[] transitionsBySource = new Map[automata.length];

    // create a mapping for each automaton
    for (int i = 0; i < transitionsBySource.length; i++) {
      AutomatonProxy automaton = automata[i];

      Map<StateProxy, List<TransitionProxy>> map = new HashMap<>();

      // add each transition to the map
      for (TransitionProxy transition : automaton.getTransitions()) {
        // add transition to list if mapping already exists
        if (map.containsKey(transition.getSource())) {
          map.get(transition.getSource()).add(transition);
        }
        // otherwise create new list and mapping
        else {
          List<TransitionProxy> list = new ArrayList<>();
          list.add(transition);
          map.put(transition.getSource(), list);
        }
      }

      transitionsBySource[i] = map;
    }

    return transitionsBySource;
  }

  /**
   * Gets a counterexample if the model was found to be not controllable
   * representing a controllability error trace. A controllability error
   * trace is a nonempty sequence of events such that all except the last
   * event in the list can be executed by the model. The last event in the list
   * is an uncontrollable event that is possible in all plant automata, but
   * not in all specification automata present in the model. Thus, the last
   * step demonstrates why the model is not controllable.
   *
   * @return A trace object representing the counterexample.
   * The returned trace is constructed for the input product DES
   * of this controllability checker and shares its automata and
   * event objects.
   */
  @Override
  public SafetyCounterExampleProxy getCounterExample() {
    // Just return a stored counterexample. This is the recommended way
    // of doing this, because we may no longer be able to use the
    // data structures used by the algorithm once the run() method has
    // finished. The counterexample can be computed by a method similar to
    // computeCounterExample() below or otherwise.
    return mCounterExample;
  }

  /**
   * Computes a counterexample.
   * This method is to be called from {@link #run()} after the model was
   * found to be not controllable, and while any data structures from
   * the controllability check that may be needed to compute the
   * counterexample are still available.
   *
   * @param end  The end state for the counter example.
   * @param last The final uncontrollable event in the counter example.
   * @return The computed counterexample.
   */
  private SafetyCounterExampleProxy computeCounterExample(StateProxy[] end, EventProxy last) {
    // the list of events to create a counter example
    LinkedList<EventProxy> eventList = new LinkedList<>();
    eventList.add(last);

    long currentStateCode = stateTupleEncoder.encode(end);
    long previousStateCode;
    long initialStateCode = stateTupleEncoder.encode(getInitialState());
    StateProxy[] currentState = stateTupleEncoder.decode(currentStateCode);
    StateProxy[] previousState;

    // trance backwards until initial state is reached
    while (currentStateCode != initialStateCode) {
      previousStateCode = stateTupleSet.getPrevious(currentStateCode);

      previousState = stateTupleEncoder.decode(previousStateCode);

      EventProxy event = getEventFromSourceAndTarget(previousState, currentState);
      eventList.addFirst(event);

      currentState = previousState;
      currentStateCode = previousStateCode;
    }

    // create counter example
    ProductDESProxyFactory desFactory = getFactory();
    String desName = getModel().getName();
    String traceName = desName + ":uncontrollable";
    return desFactory.createSafetyCounterExampleProxy(traceName, getModel(), eventList);
  }

  /**
   * Find an event that transitions from the specified source to the specified target.
   *
   * @param source The source state tuple.
   * @param target The target state tuple.
   * @return An event that transitions from the source to the target.
   */
  private EventProxy getEventFromSourceAndTarget(StateProxy[] source, StateProxy[] target) {
    // iterate over events to find an event that gets from the source to the target
    for (EventProxy event : events) {
      boolean success = true;
      // iterate over each component of the source and target tuples to see if the event works
      for (int i = 0; i < source.length; i++) {
        // check if a self loop is possible
        if (source[i] == target[i] && !eventsInAutomata[i].contains(event)) continue;

        // otherwise check if transition exists
        if (transitionExists(source[i], target[i], event, i)) continue;

        success = false;
        break;
      }

      if (success) return event;
    }

    // this should never happen
    throw new RuntimeException("No event from source to target found.");
  }

  /**
   * Check if a transition exists for a given source state, target state and event.
   *
   * @param source         The source state.
   * @param target         The target state.
   * @param event          The event.
   * @param automatonIndex The index of the automaton the source and target state are in.
   * @return If the transitions exists in the automaton.
   */
  private boolean transitionExists(StateProxy source, StateProxy target, EventProxy event, int automatonIndex) {
    // check all the transitions with the specified source
    List<TransitionProxy> transitions = transitionsBySource[automatonIndex].get(source);

    if (transitions == null) return false;

    for (TransitionProxy transition : transitions) {
      // see if transition has desired event and target
      if (transition.getEvent() == event && transition.getTarget() == target) {
        return true;
      }
    }

    return false;
  }

  /**
   * Check that counter example is valid.
   *
   * @param counterExample Counter example to check.
   * @return True if counter example is valid
   */
  private boolean isCounterExample(SafetyCounterExampleProxy counterExample) {
    ControllabilityCounterExampleChecker verifier = new ControllabilityCounterExampleChecker();

    try {
      return verifier.checkCounterExample(getModel(), counterExample);
    } catch (AnalysisException e) {
      return false;
    }
  }
}
