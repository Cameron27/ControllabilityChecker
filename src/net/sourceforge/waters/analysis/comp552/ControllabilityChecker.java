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
  private HashSet<EventProxy>[] eventsInAutomata;

  /**
   * An array of mappings between source states and transitions states for each automata.
   */
  private HashMap<StateProxy, List<TransitionProxy>>[] transitionsBySource;

  /**
   * A state tuple encoder to convert between a state tuple and the long representation of the state tuple.
   */
  private StateTupleEncoder stateTupleEncoder;

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
    ProductDESProxy model = getModel();

    setup(model);

    StateTupleSet stateTupleSet = new StateTupleSet();
    stateTupleSet.add(stateTupleEncoder.encode(getInitialState(automata)));

    StateProxy[] nextState = new StateProxy[automata.length];
    while (stateTupleSet.containsUnvisited()) {
      StateProxy[] currentState = stateTupleEncoder.decode(stateTupleSet.removeUnvisited());

      for (EventProxy event : events) {
        StateProxy temp;

        boolean success = true;
        for (int i = 0; i < nextState.length; i++) {
          AutomatonProxy automaton = automata[i];
          if (!eventsInAutomata[i].contains(event)) {
            nextState[i] = currentState[i];
          } else if ((temp = getTarget(i, currentState[i], event)) != null) {
            nextState[i] = temp;
          } else if (event.getKind() == EventKind.UNCONTROLLABLE && automaton.getKind() == ComponentKind.SPEC) {
            return false;
          } else {
            success = false;
            break;
          }
        }

        if (success) {
          stateTupleSet.add(stateTupleEncoder.encode(nextState));
        }
      }
    }

    return true;
  }

  private StateProxy getTarget(int automatonIndex, StateProxy source, EventProxy event) {
    List<TransitionProxy> possibleTransitions = transitionsBySource[automatonIndex].get(source);

    if (possibleTransitions == null) return null;

    for (TransitionProxy possibleTransition : possibleTransitions) {
      if (possibleTransition.getEvent() == event) return possibleTransition.getTarget();
    }

    return null;
  }

  private StateProxy[] getInitialState(AutomatonProxy[] automata) {
    StateProxy[] initialState = new StateProxy[automata.length];

    for (int i = 0; i < automata.length; i++) {
      AutomatonProxy automaton = automata[i];

      for (StateProxy state : automaton.getStates()) {
        if (state.isInitial()) {
          initialState[i] = state;
          break;
        }
      }
    }

    return initialState;
  }

  private void setup(ProductDESProxy model) {
    automata = getAutomata(model);
    events = getEvents(model);
    eventsInAutomata = getEventsInAutomata(automata);
    transitionsBySource = getTransitionsBySource(automata);
    stateTupleEncoder = new StateTupleEncoder(automata);
  }

  private AutomatonProxy[] getAutomata(ProductDESProxy model) {
    LinkedList<AutomatonProxy> automata = new LinkedList<>();
    for (AutomatonProxy automaton : model.getAutomata()) {
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

  private EventProxy[] getEvents(ProductDESProxy model) {
    LinkedList<EventProxy> events = new LinkedList<>();
    for (EventProxy event : model.getEvents()) {
      switch (event.getKind()) {
        case CONTROLLABLE:
          events.addFirst(event);
          break;
        case UNCONTROLLABLE:
          events.addLast(event);
      }
    }
    return events.toArray(new EventProxy[0]);
  }

  private HashSet<EventProxy>[] getEventsInAutomata(AutomatonProxy[] automata) {
    HashSet<EventProxy>[] eventsInAutomata = new HashSet[automata.length];

    for (int i = 0; i < automata.length; i++) {
      AutomatonProxy automaton = automata[i];
      eventsInAutomata[i] = new HashSet<>();
      eventsInAutomata[i].addAll(automaton.getEvents());
    }

    return eventsInAutomata;
  }

  private HashMap<StateProxy, List<TransitionProxy>>[] getTransitionsBySource(AutomatonProxy[] automata) {
    HashMap<StateProxy, List<TransitionProxy>>[] transitionsBySource = new HashMap[automata.length];

    for (int i = 0; i < transitionsBySource.length; i++) {
      AutomatonProxy automaton = automata[i];

      HashMap<StateProxy, List<TransitionProxy>> map = new HashMap<>();
      for (TransitionProxy transition : automaton.getTransitions()) {
        if (map.containsKey(transition.getSource())) {
          map.get(transition.getSource()).add(transition);
        } else {
          List<TransitionProxy> list = new ArrayList<>();
          list.add(transition);
          map.put(transition.getSource(), list);
        }
      }

      transitionsBySource[i] = map;
    }

    return transitionsBySource;
  }

  //#########################################################################
  //# Simple Access Methods

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
   * @return The computed counterexample.
   */
  private SafetyCounterExampleProxy computeCounterExample() {
    // The following creates a trace that consists of all the events in
    // the input model.
    // This code is only here to demonstrate the use of the interfaces.
    // IT DOES NOT GIVE A CORRECT COUNTEREXAMPLE!

    final ProductDESProxyFactory desFactory = getFactory();
    final ProductDESProxy des = getModel();
    final String desName = des.getName();
    final String traceName = desName + ":uncontrollable";
    final Collection<EventProxy> events = des.getEvents();
    final List<EventProxy> eventList = new LinkedList<>();
    for (final EventProxy event : events) {
      eventList.add(event);
    }
    return
        desFactory.createSafetyCounterExampleProxy(traceName, des, eventList);
  }
}
