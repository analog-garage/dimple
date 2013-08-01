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

package com.analog.lyric.dimple.graphlibrary;

import java.util.Arrays;
import java.util.HashSet;

import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.VariableBase;

public class MultiplexerCPD extends FactorGraph 
{

	private Discrete _y;
	private Discrete _a;
	private Discrete _za;
	private Discrete [] _zs;

	public MultiplexerCPD(Object [] domain, int numZs)
	{
		this(buildDomains(domain, numZs),false,false);
	}

	public MultiplexerCPD(DiscreteDomain domain, int numZs)
	{
		this(buildDomains(domain, numZs),false,false);
	}

	public MultiplexerCPD(Object [][] zDomains)
	{
		this(zDomains,false,false);
	}
	
	public MultiplexerCPD(Object [][] zDomains, boolean oneBased, boolean aAsDoubles)
	{
		super("MultiplexerCPD");
		create(zDomains,oneBased,aAsDoubles);
	}

	public MultiplexerCPD(Object [] domain, int numZs, boolean oneBased, boolean aAsDoubles)
	{
		super("MultiplexerCPD");
		create(buildDomains(domain,numZs),oneBased,aAsDoubles);
	}

	public MultiplexerCPD(DiscreteDomain [] domains)
	{
		this(domains,false,false);
	}
	
	public MultiplexerCPD(DiscreteDomain [] domains, boolean oneBased, boolean aAsDoubles)
	{
		super("MultiplexerCPD");
		create(domains,oneBased,aAsDoubles);
	}
	
	public Discrete getY()
	{
		return _y;
	}
	public Discrete getA()
	{
		return _a;
	}
	public Discrete getZA()
	{
		return _za;
	}
	public Discrete [] getZs()
	{
		return _zs;
	}
	
	
	private MultiplexerCPD create(Object [][] zDomains, boolean oneBased, boolean aAsDouble)
	{
		DiscreteDomain [] domains = new DiscreteDomain[zDomains.length];
		for (int i = 0; i < domains.length; i++)
			domains[i] = new DiscreteDomain(zDomains[i]);
		
		return create(domains,oneBased,aAsDouble);
	}
	
	private  MultiplexerCPD create(DiscreteDomain [] zDomains, boolean oneBased, boolean aAsDouble)
	{
		Discrete [] Zs = new Discrete[zDomains.length];
		int zasize = 0;
		HashSet<Object> yDomainValues = new HashSet<Object>();
		
		for (int i = 0; i < zDomains.length; i++)
		{
			Zs[i] = new Discrete(zDomains[i]);
			zasize += zDomains[i].getElements().length;
			for (int j = 0; j < zDomains[i].size(); j++)
			{
				yDomainValues.add(zDomains[i].getElements()[j]);
			}
		}
		
		
		Object [] yDomain = yDomainValues.toArray();
		Arrays.sort(yDomain);
		Discrete Y = new Discrete(yDomain);
		
		return create(Y,Zs,zasize, oneBased, aAsDouble);

	}

	private MultiplexerCPD create(Discrete Y, Discrete [] Zs, int zasize, boolean oneBased, boolean aAsDouble)
	{
		Y.setLabel("Y");
		
		java.util.Hashtable<Object, Integer> yDomainObj2index = new java.util.Hashtable<Object, Integer>();
		for (int i = 0; i < Y.getDiscreteDomain().size(); i++)
			yDomainObj2index.put(Y.getDiscreteDomain().getElements()[i], i);
		
		
		//Create a variable
		Object [] adomain = new Object[Zs.length];
		for (int i = 0; i < adomain.length; i++)
		{
			int val = oneBased ? i+1 : i;
			if (aAsDouble)
				adomain[i] = (double)val;
			else
				adomain[i] = (int)val;
		}
		Discrete A = new Discrete(adomain);		
		A.setLabel("A");
		
		addBoundaryVariables(Y);
		addBoundaryVariables(A);
		addBoundaryVariables(Zs);
		
		//Make all of those boundary variables
		VariableBase [] vars = new VariableBase[Zs.length+2];
		vars[0] = Y;
		vars[1] = A;
		for (int i = 0; i < Zs.length; i++)
			vars[i+2] = Zs[i];
		
		//Create ZA variable
		Object [] zaDomain = new Object[zasize];
		for (int i = 0; i < zaDomain.length; i++)
			zaDomain[i] = i;
		Discrete ZA = new Discrete(zaDomain);
		ZA.setLabel("ZA");
		
		//Create Z* variables
		Discrete [] Zstars = new Discrete[Zs.length];
		for (int i = 0; i < Zstars.length; i++)
		{
			Object [] domain = new Object[Zs[i].getDiscreteDomain().size() + 1];
			for (int j = 0; j < domain.length; j++)
				domain[j] = j;
			Zstars[i] = new Discrete(domain);
		}
		
		//Create ZA Y factor
		int [][] indices = new int[zasize][2];
		double [] weights = new double [zasize];

		int index = 0;
		for (int i = 0; i < Zs.length; i++)
		{
			for (int j = 0; j < Zs[i].getDiscreteDomain().size(); j++)
			{
				indices[index][0] = index;
				indices[index][1] = yDomainObj2index.get(Zs[i].getDiscreteDomain().getElements()[j]);
				
				weights[index] = 1;
				
				index++;
			}
		}
		
		Factor f = this.addFactor(indices,weights,ZA,Y);
		f.setLabel("Y2ZA");

		//Create ZA A factor
		indices = new int[zasize][2];
		weights = new double[zasize];

		index = 0;
		
		for (int i = 0; i < Zs.length; i++)
		{
			for (int j = 0; j < Zs[i].getDiscreteDomain().size(); j++)
			{
				indices[index][0] = index;
				indices[index][1] = i;
				
				weights[index] = 1;
				
				index++;
			}
		}

		f = this.addFactor(indices,weights,ZA,A);
		f.setLabel("ZA2A");
		
		//Create ZA Z* factors		
		//Create Z* Z factors
		
		for (int a = 0; a < Zs.length; a++)
		{
			Zs[a].setLabel("Z" + a);
			Zstars[a].setLabel("Z*" + a);
			
			indices = new int[zasize][2];
			weights = new double[zasize];
			
			index = 0;
			
			//Factor from ZA to Z*
			for (int i = 0; i < Zs.length; i++)
			{
				for (int j = 0; j < Zs[i].getDiscreteDomain().size(); j++)
				{
					indices[index][0] = index;
					
					if (a == i)
					{
						indices[index][1] = j;
					}
					else
					{
						int sz = Zs[a].getDiscreteDomain().size();
						indices[index][1] = sz;

					}
					
					weights[index] = 1;
					index++;
				}
			}
			
			f = this.addFactor(indices,weights,ZA,Zstars[a]);
			f.setLabel("ZA2Z*");
			
			//From Z* to Z
			indices = new int[Zs[a].getDiscreteDomain().size()*2][2];
			weights = new double[indices.length];
			
			int ds = Zs[a].getDiscreteDomain().size();
			
			for (int i = 0; i < ds; i++)
			{
				indices[i][0] = i;
				indices[ds+i][0] = ds;
				
				indices[i][1] = i;
				indices[ds+i][1] = i;
				
				weights[i] = 1;
				weights[ds+i] = 1;
			}
			
			f = this.addFactor(indices, weights,Zstars[a],Zs[a]);
			f.setLabel("Z*2Z");
		}

		this._y = Y;
		this._a = A;
		this._za = ZA;
		this._zs = Zs;
		
		return this;
	}
	public static DiscreteDomain [] buildDomains(DiscreteDomain domain, int numZs)
	{
		DiscreteDomain [] retval = new DiscreteDomain[numZs];
		for (int i = 0; i < retval.length; i++)
			retval[i] = domain;
		
		return retval;
	}

	public static DiscreteDomain [] buildDomains(Object [] domain, int numZs)
	{
		return buildDomains(new DiscreteDomain(domain), numZs);
	}
}
