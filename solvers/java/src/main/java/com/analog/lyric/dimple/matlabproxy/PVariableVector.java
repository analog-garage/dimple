/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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

package com.analog.lyric.dimple.matlabproxy;

import java.util.ArrayList;
import java.util.UUID;

import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.FactorBase;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealJoint;
import com.analog.lyric.dimple.model.VariableBase;



/*
 * This class provides vectors of Variables to allow matlab to set multiple inputs 
 * simultaneously.  This is important for performance reasons.  (MATLAB is slow to
 * manipulate lots of objects)
 */
public class PVariableVector
{
	protected PVariableBase [] _variables = new PVariableBase[0];
	
	public PVariableVector() {}
	
	// Copy constructor; use of this constructor directly allows entries that are of mixed type
	public PVariableVector(PVariableBase [] variables)
	{
		_variables = variables.clone();
	}

	
	public PVariableVector concat(Object [] varVectors, int [] varVectorIndices, int [] varIndices)
	{
		return concat(PHelpers.convertObjectArrayToVariableVectorArray(varVectors),varVectorIndices,varIndices);
	}

	public PVariableVector concat(PVariableVector [] varVectors, int [] varVectorIndices, int [] varIndices)
	{
		PVariableBase [] variables = new PVariableBase[varIndices.length];
		for (int i = 0; i < varIndices.length; i++)
		{
			variables[i] = varVectors[varVectorIndices[i]]._variables[varIndices[i]];
		}
		return new PVariableVector(variables);

//		if (varVectors[0] instanceof PRealVariableVector)			// Assumes all vectors are of the same class
//		{
//			VariableBase [] variables = new PRealVariable[varIndices.length];
//			for (int i = 0; i < varIndices.length; i++)
//			{
//				variables[i] = varVectors[varVectorIndices[i]]._variables[varIndices[i]];
//			}
//			return new PRealVariableVector(variables);
//		}
//		else
//		{
//			VariableBase [] variables = new PDiscreteVariable[varIndices.length];
//			for (int i = 0; i < varIndices.length; i++)
//			{
//				variables[i] = varVectors[varVectorIndices[i]]._variables[varIndices[i]];
//			}
//			return new PDiscreteVariableVector(variables);
//		}
	}
	public PVariableVector concat(Object [] varVectors)
	{
		return concat(PHelpers.convertObjectArrayToVariableVectorArray(varVectors));
	}
	public PVariableVector concat(PVariableVector [] varVectors)
	{
		ArrayList<PVariableBase> variables = new ArrayList<PVariableBase>();
		for (int i = 0; i < varVectors.length; i++)
		{
			for (int j =0; j < varVectors[i].size(); j++)
			{
				variables.add(varVectors[i]._variables[j]);
			}
		}
		PVariableBase [] retval = new PVariableBase[variables.size()];
		variables.toArray(retval);
		if (varVectors[0] instanceof PRealVariableVector)			// Assumes all vectors are of the same class
			return new PRealVariableVector(retval);
		else
			return new PDiscreteVariableVector(retval);
	}
	
	public PVariableVector getSlice(int [] indices)
	{
		PVariableBase [] variables = new PVariableBase[indices.length];
		for (int i = 0; i < indices.length; i++)
		{
			variables[i] = _variables[indices[i]];
		}
		if (_variables[0] instanceof PRealVariable)					// Assumes all variables are of the same class
			return new PRealVariableVector(variables);
		else
			return new PDiscreteVariableVector(variables);
	}
	
	public PVariableBase getVariable(int index)
	{
		return _variables[index];
	}
	public PVariableBase [] getVariables()
	{
		return _variables;
	}
	
	public int size()
	{
		return _variables.length;
	}
	
	//package-private
	VariableBase [] getVariableArray() 
	{
		PVariableBase[] vars = this.getVariables();
		VariableBase [] realVars;
		if (_variables.length == 0)
			realVars = new VariableBase[0];
		else if (vars[0] instanceof PRealVariable)								// Assumes all variables are of the same class
			realVars = new Real[vars.length];
		else if (vars[0] instanceof PDiscreteVariable)
			realVars = new Discrete[vars.length];
		else if (vars[0] instanceof PRealJointVariable)
			realVars = new RealJoint[vars.length];
		else
			throw new DimpleException("ack!");
		
		for (int i = 0; i < realVars.length; i++)
		{
			realVars[i] = vars[i].getModelerObject();
		}
		return realVars;
	}
	
	public String getModelerClassName() 
	{
		if (_variables.length > 0)
			return getVariableArray()[0].getModelerClassName();
		else
			return "";
	}
	
	//TODO: support all getFactors variants
	
	public PFactorBase [] getFactors(int relativeNestingDepth) 
	{
		ArrayList<PFactorBase> retval = new ArrayList<PFactorBase>();
		
		for (VariableBase v : getVariableArray())
		{
			FactorBase [] funcs = v.getFactors(relativeNestingDepth);
			PFactorBase [] tmp = PHelpers.convertToFactors(funcs);
			for (PFactorBase f : tmp)
				retval.add(f);
		}
		
		PFactorBase [] realRetVal = new PFactorBase[retval.size()];
		retval.toArray(realRetVal);
		return realRetVal;
		
	}
	
	public int [] getIds()
	{
		int [] ids = new int[_variables.length];
		for (int i = 0; i < ids.length; i++)
			ids[i] = _variables[i].getId();
		
		return ids;
	}
	
	public double [] getEnergy() 
	{
		double [] energy = new double [_variables.length];
		for (int i = 0; i < energy.length; i++)
			energy[i] = _variables[i].getEnergy();
		
		return energy;
	}
	
	public void setName(String name) 
	{
		for (PVariableBase variable : _variables)
			variable.setName(name);
	}
	
	public void setNames(String baseName) 
	{
		for(int i = 0; i < _variables.length; ++i)
		{
			_variables[i].setName(String.format("%s_vv%d", baseName, i));
		}
	}

	public void setLabel(String name) 
	{
		for (PVariableBase variable : _variables)
			variable.setLabel(name);		
	}
	
	public String [] getNames()
	{
		String [] retval = new String[_variables.length];
		for (int i = 0; i < _variables.length; i++)
		{
			retval[i] = _variables[i].getName();
		}
		return retval;
	}
	public String [] getQualifiedNames()
	{
		String [] retval = new String[_variables.length];
		for (int i = 0; i < _variables.length; i++)
		{
			retval[i] = _variables[i].getQualifiedName();
		}
		return retval;
	}
	public String [] getExplicitNames()
	{
		String [] retval = new String[_variables.length];
		for (int i = 0; i < _variables.length; i++)
		{
			retval[i] = _variables[i].getExplicitName();
		}
		return retval;
	}
	public String [] getNamesForPrint()
	{
		String [] retval = new String[_variables.length];
		for (int i = 0; i < _variables.length; i++)
		{
			retval[i] = _variables[i].getLabel();
		}
		return retval;
	}
	public String [] getQualifiedNamesForPrint()
	{
		String [] retval = new String[_variables.length];
		for (int i = 0; i < _variables.length; i++)
		{
			retval[i] = _variables[i].getQualifiedLabel();
		}
		return retval;
	}
	public UUID [] getUUIDs()
	{
		UUID [] retval = new UUID[_variables.length];
		for (int i = 0; i < _variables.length; i++)
		{
			retval[i] = _variables[i].getUUID();
		}
		return retval;
	}
	
	public Port [][] getPorts()
	{
		Port [][] ports = new Port[_variables.length][];
		for (int i = 0; i < _variables.length; i++)
		{
			ports[i] = _variables[i].getPorts();
		}
		return ports;
	}
	
	public void update() 
	{
		for (int i = 0; i < _variables.length; i++)
			_variables[i].update();
	}
	
	public void updateEdge(int portNum) 
	{
		for (int i = 0; i < _variables.length; i++)
			_variables[i].updateEdge(portNum);
	}
	
	public void setGuess(Object [] guess) 
	{
		for (int i = 0; i < guess.length; i++)
		{
			_variables[i].setGuess(guess[i]);
		}
	}
	
	public Object [] getGuess() 
	{
		Object [] retval = new Object[_variables.length];
		for (int i = 0; i < _variables.length; i++)
		{
			retval[i] = _variables[i].getGuess();
		}
		return retval;
	}
	
	public boolean isDiscrete()
	{
		return _variables[0].isDiscrete();
	}

	public boolean isReal()
	{
		return _variables[0].isReal();
	}
	
	public PDomain getDomain()
	{
		return _variables[0].getDomain();
	}
	
	public boolean isJoint()
	{
		return _variables[0].isJoint();
	}
	
	public boolean isVariable()
	{
		return true;
	}
	public boolean isFactor()
	{
		return false;
	}
	
	public boolean isGraph()
	{
		return false;
	}

}
