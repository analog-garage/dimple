/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

/**
 * 
 */
package com.analog.lyric.dimple.solvers.interfaces;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.events.ISolverEventSource;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.options.IOptionHolder;

/**
 * Base interface for solver nodes.
 * <p>
 * @author schweitz
 */
public interface ISolverNode extends ISolverFactorGraphChild, IOptionHolder, ISolverEventSource
{
	/**
	 * Perform update of this node.
	 * <p>
	 * For belief propogation (BP) solvers, this will update the outgoing messages for
	 * all edges attached to this node.
	 */
	public void update() ;
	
	/**
	 * Perform update of this node with respect to a single edge.
	 * <p>
	 * For belief propogation (BP) solvers, this will update the outgoing message for
	 * the specified edge.
	 * <p>
	 * @param siblingNumber specifies which edge to update. Must be a non-negative value less
	 * than {@link #getSiblingCount()}.
	 */
	public void updateEdge(int siblingNumber) ;
	
	/**
	 * Initialize the solver node.
	 * <p>
	 * This method is called before solve.
	 */
	@Override
	public void initialize() ;
	
	/**
	 * Returns solver edge state, if any.
	 * 
	 * @param siblingNumber identifies which edge to return. Must be non-negative and less than {@link #getSiblingCount()}
	 * @since 0.08
	 */
	public @Nullable ISolverEdgeState getSiblingEdgeState(int siblingNumber);
	
	/**
	 * Returns solver node attached to this node through edge with given edge number.
	 * @param siblingNumber is non-negative value less than {@link #getSiblingCount()}
	 * @since 0.06
	 */
	public ISolverNode getSibling(int siblingNumber);
	
	/**
	 * Returns number of solver nodes attached to this one.
	 * <p>
	 * This should be the same as the count on the corresponding {@linkplain #getModelObject() model node}.
	 * <p>
	 * @since 0.06
	 * @see #getSibling(int)
	 */
	public int getSiblingCount();
	
	/**
	 * @deprecated as of release 0.08, scoring should be done via
	 * {@link com.analog.lyric.dimple.data.DataStack#computeTotalEnergy() DataStack.computeTotalEnergy()}
	 */
	@Deprecated
	public double getScore() ;
	
    public double getInternalEnergy() ;
    public double getBetheEntropy() ;
    
    /**
     * Return the model object associated with this solver node.
     */
	@Override
	public INode getModelObject();

    /*--------------------
     * Deprecated methods
     */
    
    /**
     * @deprecated Instead use {@link #getSiblingEdgeState(int)} and {@link ISolverEdgeState#getFactorToVarMsg()} or
     * {@link ISolverEdgeState#getVarToFactorMsg()}.
     */
    @Deprecated
    public @Nullable Object getInputMsg(int portIndex);
    
    /**
     * @deprecated Instead use {@link #getSiblingEdgeState(int)} and {@link ISolverEdgeState#getFactorToVarMsg()} or
     * {@link ISolverEdgeState#getVarToFactorMsg()}.
     */
    @Deprecated
    public @Nullable Object getOutputMsg(int portIndex);
    
    /**
     * @deprecated Instead use {@link #getSiblingEdgeState(int)} and {@link ISolverEdgeState#getFactorToVarMsg()} or
     * {@link ISolverEdgeState#getVarToFactorMsg()}.
     */
    @Deprecated
    public void setInputMsg(int portIndex,Object obj);
    
    /**
     * @deprecated Instead use {@link #getSiblingEdgeState(int)} and {@link ISolverEdgeState#getFactorToVarMsg()} or
     * {@link ISolverEdgeState#getVarToFactorMsg()}.
     */
    @Deprecated
    public void setOutputMsg(int portIndex,Object obj);
    
    /**
     * @deprecated Instead use {@link #getSiblingEdgeState(int)} and {@link ISolverEdgeState#getFactorToVarMsg()} or
     * {@link ISolverEdgeState#getVarToFactorMsg()}.
     */
    @Deprecated
    public void setInputMsgValues(int portIndex,Object obj);
    
    /**
     * @deprecated Instead use {@link #getSiblingEdgeState(int)} and {@link ISolverEdgeState#getFactorToVarMsg()} or
     * {@link ISolverEdgeState#getVarToFactorMsg()}.
     */
    @Deprecated
    public void setOutputMsgValues(int portIndex,Object obj);
}
