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

import static java.lang.String.*;

import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.events.IDataEventSource;
import com.analog.lyric.dimple.events.IDimpleEventListener;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Equality;
import com.analog.lyric.dimple.factorfunctions.core.IUnaryFactorFunction;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Ids;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.core.NodeType;
import com.analog.lyric.dimple.model.core.VariablePort;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.factors.FactorBase;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.SNode;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
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
	
    protected @Nullable IDatum _prior = null;
    
	protected String _modelerClassName;
	private final Domain _domain;
    
    public static Comparator<Variable> orderById = new Comparator<Variable>() {
		@Override
		@NonNullByDefault(false)
		public int compare(Variable var1, Variable var2)
		{
			return Longs.compare(var1.getGlobalId(), var2.getGlobalId());
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
		super(Ids.INITIAL_VARIABLE_ID);
		
		_modelerClassName = modelerClassName;
		_domain = domain;
	}
	
	protected Variable(Variable that)
	{
		super(that);
		_modelerClassName = that._modelerClassName;
		_domain = that._domain;
		IDatum prior = that._prior;
		_prior = prior != null ? prior.clone() : null;
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
	public final VariablePort getPort(int siblingNumber)
	{
		return new VariablePort(this, siblingNumber);
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
		final FactorGraph fg = getParentGraph();
		if (fg != null)
		{
			final ISolverFactorGraph sfg = fg.getSolver();
			if (sfg != null)
			{
				return sfg.getSolverVariable(this);
			}
		}
		
		return null;
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
    
	/*--------------
	 * Node methods
	 */
	
	@Override
	protected int getEventMask()
	{
		return super.getEventMask() | EVENT_MASK;
	}
	
	/*------------------
	 * Variable methods
	 */
	
	/**
	 * Casts this object to a {@link Discrete}.
	 * @throws ClassCastException if this is not an instance of {@link Discrete}.
	 */
	public Discrete asDiscreteVariable()
	{
		return (Discrete)this;
	}

	/**
	 * Get prior value associated with variable, if any.
	 * <p>
	 * This may either be a {@link Value} object specifying a fixed value (i.e. making
	 * the variable a named constant) or a {@link IUnaryFactorFunction} or {@link IParameterizedMessage}
	 * representing a prior distribution.
	 * <p>
	 * Note: this attribute replaces the various "fixed value" and "input" methods that
	 * will eventually be phased out.
	 * <p>
	 * @since 0.08
	 * @see #getInputObject()
	 * @see #getFixedValueObject()
	 */
	public @Nullable IDatum getPrior()
	{
		return _prior;
	}
	
	/**
	 * Get prior if it is a {@link IUnaryFactorFunction}, else null.
	 * @since 0.08
	 * @see #getPrior()
	 */
	public final @Nullable IUnaryFactorFunction getPriorFunction()
	{
		final IDatum prior = _prior;
		return prior instanceof IUnaryFactorFunction ? (IUnaryFactorFunction)prior : null;
	}
	
	/**
	 * Get prior if it is a {@link Value}, else null.
	 * @since 0.08
	 * @see #getPrior()
	 */
	public final @Nullable Value getPriorValue()
	{
		final IDatum prior = _prior;
		return prior instanceof Value ? (Value)prior : null;
	}
	
	/**
	 * Associates a prior with the variable.
	 * <p>
	 * Sets the value of the {@linkplain #getPrior prior}.
	 * <p>
	 * @param prior may be one of the following:
	 * <ul>
	 * <li>{@code null} to remove any existing prior
	 * <li>a {@link Value} object specifying fixed value for variable
	 * <li>a value of the variable's {@linkplain #getDomain domain} specifying a fixed value
	 * <li>a {@link IParameterizedMessage} appropriate to the variable's type
	 * <li>any {@link IUnaryFactorFunction} appropriate to the variables domain. However, note that
	 * not all solvers currently support such priors. They may be safely used with the Gibbs solver.
	 * </ul>
	 * @since 0.08
	 */
	public @Nullable IDatum setPrior(@Nullable Object prior)
	{
		final IDatum priorPrior = _prior;
		
		if (prior == null || prior instanceof IDatum)
		{
			_prior = (IDatum)prior;
		}
		else if (_domain.inDomain(prior))
		{
			_prior = Value.create(_domain, prior);
		}
		else
		{
			throw new ClassCastException(format("'%s' is neither an %s nor a member of variable's domain",
				prior,
				// Use Class instead of hard-coding name so that we can rename it easily
				IDatum.class.getSimpleName()));
		}
		
		final IDatum newPrior = _prior;
		
    	final ISolverVariable svar = getSolver();
    	if (svar != null)
    	{
			svar.setInputOrFixedValue(priorToInput(newPrior), priorToFixedValue(newPrior));
    	}
    	
    	priorChanged(priorPrior, newPrior);
    
    	return priorPrior;
	}
	
	protected @Nullable Object priorToFixedValue(@Nullable IDatum prior)
	{
		return prior instanceof Value ? ((Value)prior).getObject() : null;
	}
	
	protected @Nullable Object priorToInput(@Nullable IDatum prior)
	{
		return prior instanceof Value ? null : prior;
	}
	
	private void priorChanged(@Nullable IDatum priorPrior, @Nullable IDatum newPrior)
	{
    	final int eventFlags = getChangeEventFlags();
    	
    	if (eventFlags != NO_CHANGE_EVENT)
    	{
    		if ((eventFlags & FIXED_VALUE_CHANGE_EVENT) != 0 &&
    			(newPrior instanceof Value || priorPrior instanceof Value))
    		{
    			raiseEvent(new VariableFixedValueChangeEvent(this, priorToFixedValue(priorPrior),
    				priorToFixedValue(newPrior)));
    		}
    		if ((eventFlags & INPUT_CHANGE_EVENT) != 0)
    		{
    			final Object prevInput = priorPrior instanceof Value ? null : priorToInput(priorPrior);
    			final Object newInput = newPrior instanceof Value ? null : priorToInput(newPrior);
    			if (prevInput != newInput)
    			{
    				raiseEvent(new VariableInputChangeEvent(this, prevInput, newInput));
    			}
    		}
    	}
	}

	public Domain getDomain()
	{
		return _domain;
	}
	
    public @Nullable Object getInputObject()
    {
    	IDatum datum = getPrior();
    	return datum instanceof Value ? null : datum;
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
		setPrior(other.getPrior());
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
	public final void setFixedValueFromObject(@Nullable Object value)
	{
		setPrior(value);
	}
	
	// REFACTOR: this is not a good name - has different semantics for discrete and non-discrete
	// For Discrete this returns the index of the fixed value, for non-discrete it returns the actual fixed
	// value.
	public @Nullable Object getFixedValueObject()
	{
		IDatum datum = getPrior();
    	if (datum instanceof Value)
    	{
    		return ((Value) datum).getObject();
    	}
    	
    	return null;
	}
	
	public void setFixedValueObject(@Nullable Object value)
	{
		setPrior(value != null ? Value.create(_domain, value) : null);
	}
	
    public void setInputObject(@Nullable Object value)
    {
    	setPrior(value);
    }
    
    // For setting the variable to a fixed value in lieu of an input
	public final boolean hasFixedValue()
	{
		return _prior instanceof Value;
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
    			EdgeState edge = factor.getSiblingEdgeState(j);
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
    
	/*--------------------
	 * Deprecated methods
	 */
	
    /**
	 * @category internal
	 */
	@Deprecated
	@Internal
	public void setSolver(@Nullable ISolverVariable svar)
	{
		throw new UnsupportedOperationException("Variable.setSolver no longer supported");
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
    @Override
	@Internal
    public ISolverVariable requireSolver(String methodName)
    {
    	final ISolverVariable svar = getSolver();
    	if (svar == null)
    	{
    		throw new NullPointerException(String.format("solver must be set before using '%s'", methodName));
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
