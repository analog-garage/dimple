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
 * @author schweitz
 *
 */
public interface ISolverNode extends IOptionHolder, ISolverEventSource
{
	//Update all outgoing messages
	public void update() ;
	
	//Update output messages for the specified port number
	public void updateEdge(int outPortNum) ;
	
	/**
	 * Initialize the solver node.
	 * <p>
	 * This method is called before solve. It can be used to reset messages.
	 */
	public void initialize() ;
	
	//This method is called for every edge during initialize.
	public void resetEdgeMessages(int portNum);
	
	/**
	 * Returns the solver factor graph to which this node belongs.
	 */
	public @Nullable ISolverFactorGraph getParentGraph();
	
	/**
	 * Gets the highest level solver graph to which this node belongs.
	 */
	public @Nullable ISolverFactorGraph getRootGraph();

	/**
	 * Returns solver node attached to this node through edge with given index.
	 * @param edge
	 * @since 0.06
	 * @see #getSiblingCount()
	 */
	public ISolverNode getSibling(int edge);
	
	/**
	 * Returns number of solver nodes attached to this one.
	 * @since 0.06
	 * @see #getSibling(int)
	 */
	public int getSiblingCount();
	
	public double getScore() ;
    public double getInternalEnergy() ;
    public double getBetheEntropy() ;
    
    /**
     * Return the model object associated with this solver node.
     */
    public @Nullable INode getModelObject();
    
    //Returns the input messages for a specified port
    public @Nullable Object getInputMsg(int portIndex);
    
    //Returns the output message for a specified port.
    public @Nullable Object getOutputMsg(int portIndex);
    
    //Replace the input message with the specified object
    public void setInputMsg(int portIndex,Object obj);
    
    //Replace the output message with the specified object
    public void setOutputMsg(int portIndex,Object obj);
    
    //Set the input message values to be the same as the specified obj
    public void setInputMsgValues(int portIndex,Object obj);
    
    //Set the output message values to be the same as the specified obj
    public void setOutputMsgValues(int portIndex,Object obj);
    
    //Move messages from the other node's port to this node's port.
    public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum);
    
}
