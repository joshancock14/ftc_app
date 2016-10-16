package org.firstinspires.ftc.teamcode;

import java.util.ArrayList;
import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.robotcore.robot.Robot;

/**
 * Created by Derek Williams of team 3965 on 10/9/2016.
 */

class StateMachine {
	private State activeState = null;
	private boolean stateHasStarted = false;
	private boolean active = true;
	public RobotBase robot;
	private ArrayList<State> states = new ArrayList<State>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();

	StateMachine(RobotBase robot) {
		this.robot = robot;
	}

	public boolean isActive(){
		return active;
	}

	public State getState(String name) {
		for(State state : states) {
			if(state.name == name) {
				return state;
			}
		}
		return null;
	}

	public void stop() {
		activeState.stop();
	}
	
	public void setActiveState(State state) {
		activeState = state;
	}
	
	public void setActiveState(String name) {
		setActiveState(getState(name));
	}
	
	public void add(State state) {
		state.machine = this;
		states.add(state);
	}
	
	public void add(State[] states) {
		for(State state : states) {
			add(state);
		}
	}

	public void add(Transition transition) {
		transition.machine = this;
		transitions.add(transition);
	}

	public void add(Transition[] transitions) {
		for(Transition transition : transitions) {
			add(transition);
		}
	}

	public boolean shouldExitState() {
		boolean shouldExit = false;
		for(Transition transition : transitions) {
				shouldExit = shouldExit || transition.test();
		}
		return shouldExit;
	}

	public ArrayList<Transition> getPossibleTransitions() {
		ArrayList<Transition> possibleTransitions = new ArrayList<Transition>();
		for(Transition transition : transitions) {
			if(activeState == transition.getFromState()) {
				possibleTransitions.add(transition);
			}
		}
		return possibleTransitions;
	}

	public Transition getTransitionToTrigger() {
		for(Transition transition : getPossibleTransitions()) {
			if(transition.test()){
				return transition;
			}
		}
		return null;
	}

	public void triggerTransition(Transition transition) {
		setActiveState(transition.getToState());
		stateHasStarted = false;
	}

	private void handleTransitions() {
		Transition transitionToTrigger = getTransitionToTrigger();
		if(transitionToTrigger != null) {
			DbgLog.msg("Leaving "+ activeState.name +" State");
			activeState.stop();
			triggerTransition(transitionToTrigger);
		}
	}

	private void handleStartingStates() {
		if(!stateHasStarted) {
			DbgLog.msg("Entering "+ activeState.name +" State");
			activeState.start();
			stateHasStarted = true;
		}
	}
	
	public void step(){
		if(activeState != null) {
			active = true;
			handleStartingStates();
			if(!shouldExitState() && !stateHasStarted) {
				activeState.loop();
				handleTransitions();
			}
		}
		else {
			active = false;
		}
	}
}

abstract class State {
	protected String name = null;
	protected StateMachine machine;
	protected RobotBase robot = null;

	public double getProgress() {
		return 0.0;
	}
	
	public abstract void start();
	
	public abstract void loop();
	
	public abstract void stop();
}

abstract class Transition {
	protected StateMachine machine;
	protected String fromStateName;
	protected String toStateName;

	final State getFromState() {
		return machine.getState(fromStateName);
	}

	final State getToState() {
		return machine.getState(toStateName);
	}

	abstract public boolean test();
}