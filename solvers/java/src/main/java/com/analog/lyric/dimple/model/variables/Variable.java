/*******************************************************************************
*   Copyright 2012-2014 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.model.variables;

import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.events.IDataEventSource;
import com.analog.lyric.dimple.events.IDimpleEventListener;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Equality;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorGraphEdgeState;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.core.NodeId;
import com.analog.lyric.dimple.model.core.NodeType;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.factors.FactorBase;
import com.analog.lyric.dimple.solvers.core.SNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.util.misc.Internal;
import com.google.common.primitives.Longs;

/**
 * Base class for model variables in Dimple
 * 
 * @since 0.07
 */
public abstract class Variable extends Node implements Cloneable, IDataEventSource
{
	/*-----------
	 * Constants
	 */
	
	/**
	 * {@link #_topologicalFlags} value used by {@link #isDeterministicInput()}
	 */
    private static final byte DETERMINISTIC_INPUT = 0x04;
	/**
	 * {@link #_topologicalFlags} value used by {@link #isDeterministicOutput()}
	 */
    private static final byte DETERMINISTIC_OUTPUT = 0x08;
    
    protected static final int RESERVED_FLAGS         = 0xFFFF0000;
    
    private static final int EVENT_MASK               = 0x00070000;
    
    private static final int CHANGE_EVENT_KNOWN       = 0x00010000;
    private static final int FIXED_VALUE_CHANGE_EVENT = 0x00020000;
    private static final int INPUT_CHANGE_EVENT       = 0x00040000;
    private static final int CHANGE_EVENT_MASK        = 0x00070000;
    private static final int NO_CHANGE_EVENT          = 0x00010000;
    
    /*-------
	 * State
	 */
	
	protected @Nullable Object _input = null;
	protected @Nullable Object _fixedValue = null;
	protected String _modelerClassName;
	protected @Nullable ISolverVariable _solverVariable = null;
	private final Domain _domain;
    
    public static Comparator<Variable> orderById = new Comparator<Variable>() {
		@Override
		@NonNullByDefault(false)
		public int compare(Variable var1, Variable var2)
		{
			return Longs.compare(var1.getId(), var2.getId());
		}
    };

    /*--------------
     * Construction
     */
    
	public Variable(Domain domain)
	{
		this(domain, "Variable");
	}
	
	public Variable(Domain domain, String modelerClassName)
	{
		super(NodeId.INITIAL_VARIABLE_ID);
		
		_modelerClassName = modelerClassName;
		_domain = domain;
	}
	
	protected Variable(Variable that)
	{
		super(that);
		_modelerClassName = that._modelerClassName;
		_domain = that._domain;
		// FIXME
		_input = that._input;
		_fixedValue = that._fixedValue;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public abstract @NonNull Variable clone();

	/*---------------
	 * INode methods
	 */
	
	@Override
	public final Variable asVariable()
	{
		return this;
	}
	
	@Override
	public final boolean isVariable()
	{
		return true;
	}
	
	@Override
	public NodeType getNodeType()
	{
		return NodeType.VARIABLE;
	}

	@Override
	public String getClassLabel()
    {
    	return "Variable";
    }
    
    @Override
    public Factor getSibling(int i)
    {
    	// Variables should only be connected to factors
    	return (Factor)super.getSibling(i);
    }
    
	@Override
	public List<Factor> getSiblings()
	{
		@SuppressWarnings("unchecked")
		List<Factor> siblings = (List<Factor>)super.getSiblings();
		return siblings;
	}
	
    /**
     * Returns the solver-specific variable instance associated with this model variable if any.
     */
	@Override
	public @Nullable ISolverVariable getSolver()
	{
		return _solverVariable;
	}
	
	@Override
	public double getScore()
	{
		return requireSolver("getScore").getScore();
	}

	@Override
	public double getBetheEntropy()
	{
		return requireSolver("getBetheEntropy").getBetheEntropy();
	}
	
	@Override
	public double getInternalEnergy()
	{
		return requireSolver("getInternalEnergy").getInternalEnergy();
		
	}
	
	@Override
	public void initialize(int portNum)
	{
		final ISolverVariable svar = getSolver();
		if (svar != null)
		{
			svar.resetEdgeMessages(portNum);
		}
	}
    
	/**
	 * Model-specific initialization for variables.
	 * <p>
	 * Clears {@link #isDeterministicInput()} and {@link #isDeterministicOutput()}.
	 * Does NOT invoke solver variable initialize.
	 */
    @Override
	public void initialize()
    {
    	super.initialize();
    }
    
	@Override
	public void update()
	{
		requireSolver("update").update();
	}

	@Override
	public void updateEdge(int outPortNum)
	{
		requireSolver("updateEdge").updateEdge(outPortNum);
	}

	/*--------------
	 * Node methods
	 */
	
	@Override
	protected int getEventMask()
	{
		return super.getEventMask() | EVENT_MASK;
	}
	
	/*----------------------
	 * VariableBase methods
	 */
	
	/**
	 * Casts this object to a {@link Discrete}.
	 * @throws ClassCastException if this is not an instance of {@link Discrete}.
	 */
	public Discrete asDiscreteVariable()
	{
		return (Discrete)this;
	}
	
	public Domain getDomain()
	{
		return _domain;
	}
	
    public @Nullable Object getInputObject()
    {
    	return _input;
    }

    /**
     * Returns the solver-specific variable instance associated with this model variable if it is
     * an instance of the specified {@code solverVariableClass}, otherwise returns null.
     */
	public @Nullable <T extends ISolverVariable> T getSolverIfType(Class<? extends T> solverVariableClass)
	{
		final ISolverVariable svar = getSolver();
		T result = null;
		
		if (svar != null && solverVariableClass.isAssignableFrom(svar.getClass()))
		{
			result = solverVariableClass.cast(svar);
		}
		
		return result;
	}
	
    /**
     * Returns the solver-specific variable instance associated with this model variable if it is
     * an instance of the specified {@code solverVariableClass} and has {@link SNode#getParentGraph()} equal to
     * {@code solverGraph}, otherwise returns null.
     */
	public @Nullable <T extends ISolverVariable> T getSolverIfTypeAndGraph(
		Class<? extends T> solverVariableClass,
		ISolverFactorGraph solverGraph)
	{
		T svar = getSolverIfType(solverVariableClass);
		if (svar != null && svar.getParentGraph() != solverGraph)
		{
			svar = null;
		}
		return svar;
	}

	/**
	 * True if variable is an input to a directed deterministic function.
	 * <p>
	 * This attribute is not valid until after graph initialization has occurred
	 * (see {@link FactorGraph#initialize()}).
	 * 
	 * @since 0.05
	 */
    public final boolean isDeterministicInput()
    {
    	return isFlagSet(DETERMINISTIC_INPUT);
    }

	/**
	 * True if variable is an output from a directed deterministic function.
	 * <p>
	 * This attribute is not valid until after graph initialization has occurred
	 * (see {@link FactorGraph#initialize()}).
	 * 
	 * @since 0.05
	 */
    public final boolean isDeterministicOutput()
    {
    	return isFlagSet(DETERMINISTIC_OUTPUT);
    }
	
   public void setGuess(@Nullable Object guess)
    {
    	requireSolver("setGuess").setGuess(guess);
    }

    public boolean guessWasSet()
    {
    	ISolverVariable svar = getSolver();
    	return svar != null && svar.guessWasSet();
    }
    
    public @Nullable Object getGuess()
    {
    	return requireSolver("getGuess").getGuess();
    }
    
	public void moveInputs(Variable other)
	{
		_input = other._input;
		_fixedValue = other._fixedValue;
		requireSolver("moveInputs").setInputOrFixedValue(_input,_fixedValue);
	}

	/**
	 * @category internal
	 */
	@Internal
	public void createSolverObject(@Nullable ISolverFactorGraph factorGraph)
	{
		if (factorGraph != null)
		{
			factorGraph.getSolverVariable(this, true);
		}
		else
		{
			_solverVariable = null;
		}
	}
	
	/**
	 * @category internal
	 */
	@Internal
	public void setSolver(@Nullable ISolverVariable svar)
	{
		_solverVariable = svar;
	}
	
	/**
	 * Returns fixed value of variable or null if not fixed.
	 * @since 0.07
	 */
	public abstract @Nullable Object getFixedValueAsObject();

	/**
	 * Sets variable to a fixed value.
	 * @since 0.07
	 */
	public abstract void setFixedValueFromObject(@Nullable Object value);
	
	// REFACTOR: this is not a good name - has different semantics for discrete and non-discrete
	// For Discrete this returns the index of the fixed value, for non-discrete it returns the actual fixed
	// value.
	public @Nullable Object getFixedValueObject()
	{
		return _fixedValue;
	}
	
	// FIXME: we do not allow null values in any currently supported domain, so we should make
	// setting this to null the same as removing the fixed value
	public void setFixedValueObject(@Nullable Object value)
	{
		setInputOrFixedValue(value, null);
	}
	
    public void setInputObject(@Nullable Object value)
    {
    	setInputOrFixedValue(null, value);
    }
    
    // For setting the variable to a fixed value in lieu of an input
	public final boolean hasFixedValue()
	{
		return _fixedValue != null;
	}
	
    public String getModelerClassName()
    {
    	return _modelerClassName;
    }
        
    public @Nullable Object getBeliefObject()
    {
    	final ISolverVariable svar = getSolver();
    	if (svar != null)
    		return svar.getBelief();
    	else
    		return getInputObject();
    }
   
    
    public Factor [] getFactors()
    {
    	return getFactorsFlat();
    }
    
	public FactorBase [] getFactors(int relativeNestingDepth)
	{
		int nSiblings = getSiblingCount();
		FactorBase [] retval = new FactorBase[nSiblings];
		
		for (int i = 0; i < nSiblings; i++)
		{
			retval[i] = (FactorBase)getConnectedNode(relativeNestingDepth,i);
		}
		return retval;
	}

	public FactorBase [] getFactorsTop()
	{
		return getFactors(0);
	}
	
	public Factor [] getFactorsFlat()
	{
		int nSiblings = getSiblingCount();
		Factor [] retval = new Factor[nSiblings];
		for (int i = 0; i < nSiblings; i++)
		{
			retval[i] = (Factor)getConnectedNodeFlat(i);
		}
		return retval;
		
	}
	
	@Internal
    public Variable split(FactorGraph fg,Factor [] factorsToBeMovedToCopy)
    {
    	//create a copy of this variable
    	Variable mycopy = clone();
    	mycopy.createSolverObject(null);
    	mycopy.setInputObject(null);
    	mycopy.setName(null);
    	
    	fg.addFactor(new Equality(), this,mycopy);
    	
    	//for each factor to be moved
    	for (int i = 0; i < factorsToBeMovedToCopy.length; i++)
    	{
    		Factor factor = factorsToBeMovedToCopy[i];
    		//Replace the connection from this variable to the copy in the factor
    		for (int j = 0, endj = factor.getSiblingCount(); j < endj; j++)
    		{
    			FactorGraphEdgeState edge = factor.getSiblingEdgeState(j);
    			if (edge.getVariable(fg) == this)
    			{
    				fg.replaceEdge(factor, j, mycopy);
    			}
    		}
    		
    	}
    	
    	//set the solvers to null for this variable, the copied variable, and all the factors that were moved.
    	ISolverFactorGraph sfg = fg.getSolver();
    		
    	if (sfg != null)
    	{
			createSolverObject(fg.getSolver());
			mycopy.createSolverObject(fg.getSolver());
	    	
	    	for (int i = 0; i < factorsToBeMovedToCopy.length; i++)
	    		factorsToBeMovedToCopy[i].createSolverObject(fg.getSolver());
    	}
    	return mycopy;
    }
    
    /*-------------------
     * Internal methods
     */
    
    /**
     * Creates a new variable that combines the domains of this variable with additional {@code variables}.
     * <p>
     * For use by {@link FactorGraph#join(Variable...)}. Currently only supported for {@link Discrete}
     * variables.
     * <p>
     * @param variables specifies at least one additional variables to join with this one. As a convenience, this
     * may begin with this variable, in which case there must be at least one other variable.
     * 
     * @category internal
     */
    @Internal
    public Variable createJointNoFactors(Variable ... variables)
    {
    	throw new DimpleException("not implemented");
    }
    
    /**
     * Returns solver variable or throws an exception if not yet set.
     * <p>
     * For internal use only.
     * <p>
     * @since 0.06
     */
    @Internal
    public ISolverVariable requireSolver(String methodName)
    {
    	final ISolverVariable svar = getSolver();
    	if (svar == null)
    	{
    		throw new DimpleException("solver must be set before using '%s'", methodName);
    	}
    	return svar;
    }
    
   /**
     * Sets {@link #isDeterministicInput()} to true.
     * 
     * @since 0.05
     * 
     * @category internal
     */
    @Internal
    public final void setDeterministicInput()
    {
    	setFlags(DETERMINISTIC_INPUT);
    }
    
    /**
     * Sets {@link #isDeterministicOutput()} to true.
     * 
     * @since 0.05
     * 
     * @category internal
     */
    @Internal
    public final void setDeterministicOutput()
    {
    	setFlags(DETERMINISTIC_OUTPUT);
    }
    
    /*-----------------
     * Private methods
     */
    
    protected final void setInputOrFixedValue(@Nullable Object newFixedValue, @Nullable Object newInput)
    {
    	final Object prevInput = _input;
    	final Object prevFixedValue = _fixedValue;
    	
    	_fixedValue = newFixedValue;
    	_input = newInput;
    	
    	final ISolverVariable svar = getSolver();
    	if (svar != null)
    	{
			svar.setInputOrFixedValue(_input,_fixedValue);
    	}
    
    	final int eventFlags = getChangeEventFlags();
    	
    	if (eventFlags != NO_CHANGE_EVENT)
    	{
    		if ((eventFlags & FIXED_VALUE_CHANGE_EVENT) != 0 &&
    			(newFixedValue != null || prevFixedValue != null))
    		{
    			raiseEvent(new VariableFixedValueChangeEvent(this, prevFixedValue, _fixedValue));
    		}
    		if ((eventFlags & INPUT_CHANGE_EVENT) != 0 && prevInput != _input)
    		{
    			raiseEvent(new VariableInputChangeEvent(this, prevInput, _input));
    		}
    	}
    }
    
    private int getChangeEventFlags()
    {
    	final int prevFlags = _flags & CHANGE_EVENT_MASK;
    	
    	if ((prevFlags & CHANGE_EVENT_KNOWN) != 0)
    	{
    		return prevFlags;
    	}
    	
    	int flags = 0;
    	
    	final IDimpleEventListener listener = getEventListener();
    	if (listener != null)
    	{
    		if (listener.isListeningFor(VariableInputChangeEvent.class, this))
    		{
    			flags |= INPUT_CHANGE_EVENT;
    		}
    		if (listener.isListeningFor(VariableFixedValueChangeEvent.class, this))
    		{
    			flags |= FIXED_VALUE_CHANGE_EVENT;
    		}
    	}

    	setFlagValue(CHANGE_EVENT_MASK, flags);
    
		return flags;
    }
}
