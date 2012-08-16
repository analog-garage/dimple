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

package com.analog.lyric.dimple.solvers.sumproduct;

import java.util.HashMap;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.VariableBase;


/*
 * This class provides an implementation for FiniteField variables (with a characteristic of 2).  
 * It does not modify
 * the update algorithm but provides dlog and power tables for the FiniteField function classes.
 * The dlog (discrete log) table can be used for calculating the discrete log of the variable.
 * The power table does the opposite.
 * 
 * There is a unique pair of lookup tables for each primitive polynomial.  As a result we cache
 * the lookup tables for each unique primitive polynomial.
 */
public class FiniteFieldVariable extends SVariable 
{
	//global cache of lookup tables.
	private static HashMap<Integer,LookupTables> _poly2tables = new HashMap<Integer,LookupTables>();
	
	//A pointer to the correct lookup table for this variable.
	private LookupTables _tables = null;
	private int _numBits;


	public FiniteFieldVariable(VariableBase var) 
	{
		super(var);
		
		double [] primPoly = (double[])var.getProperty("primitivePolynomial");
		
		setPrimitivePolynomial(primPoly);
	}
	
	public LookupTables getTables()
	{
		return _tables;
	}
	
	//Converts a double array of bits to the resulting integer
	public static int convertDoubleArray2poly(double [] poly)
	{
		int key = 0;
		int max = poly.length-1;
		
		for (int i = 0; i < poly.length; i++)
		{
			key = key | ((int)poly[i]) << max-i;
		}
		return key;
	}

	public int getNumBits()
	{
		return _numBits;
	}
	
	//This gets called once, shortly after the Variable is instantiated.
	public void setPrimitivePolynomial(double [] poly) 
	{
		int key = convertDoubleArray2poly(poly);
		_numBits = poly.length-1;
		
		if (_tables != null)
			throw new DimpleException("poly already set");
		else
		{
			//Create the tables if they don't exist.
			if (!_poly2tables.containsKey(key))
				_poly2tables.put(key,new LookupTables(key, poly.length-1));

			//save the tables.
			_tables = _poly2tables.get(key);
		}
		
	}
	
	/*
	 * The LookupTables class simply contains the polynomial and the dlog and power tables.
	 */
	public class LookupTables
	{
		private int _poly;
		private int [] _powerTable;
		private int [] _dlogTable;
		
		
		//The constructor constructs the tables. 
		public LookupTables(int poly, int polySize) 
		{			
			_poly = poly;
						
			//Num entries is 2^polySize-1
			int numEntries = (1 << polySize)-1;
			
			_powerTable = new int [numEntries];
			_dlogTable = new int [numEntries+1];
			
			_dlogTable[0] = -1; //Really this is undefined
			_powerTable[0] = 1;
			
			int current = _powerTable[0];

			//Calculate the powers using the LFSR of the primite polynomial
			for (int i = 1; i < numEntries; i++)
			{
				//LFSR equation
				current = ((current >> (polySize-1))*_poly ^ (current << 1)) & numEntries;
				
				//If we get back to 1 before ending the sequence, this is not a primitive 
				//polynomial.
				if (current == 1)
					throw new DimpleException("polynomial is not irreducible");
				
				//Store the entry in the tables.
				_dlogTable[current] = i;
				_powerTable[i] = current;
			}
			
		}
		
		
		public int [] getPowerTable()
		{
			return _powerTable;
		}
		
		public int [] getDlogTable()
		{
			return _dlogTable;
		}
		
		//Useful for debug
		public void prettyPrint(int current,int polySize)
		{
			for (int j = 0; j < polySize+1;j++)
			{
				if ((current & (1 << (polySize-j))) != 0)
				{
					System.out.print("1");
				}
				else
					System.out.print("0");
			}
			System.out.print("\n");

		}
		
		public int getPoly()
		{
			return _poly;
		}
		
	}
	

}
