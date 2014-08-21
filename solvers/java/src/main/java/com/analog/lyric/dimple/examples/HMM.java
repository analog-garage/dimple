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

package com.analog.lyric.dimple.examples;

import static java.util.Objects.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

import cern.colt.Arrays;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

public class HMM
{

	@NonNullByDefault
	public static class TransitionFactorFunction extends FactorFunction
	{
		public TransitionFactorFunction()
		{
			super("TransitionFactorFunction");
		}
		
		@Override
		public double evalEnergy(Object ... args)
		{
			String state1 = (String)args[0];
			String state2 = (String)args[1];
			double value;
			
			if (state1.equals("sunny"))
			{
				if (state2.equals("sunny"))
				{
					value = 0.8;
				}
				else
				{
					value = 0.2;
				}
			}
			else
			{
				value = 0.5;
			}
			return -Math.log(value);
		}
	}
	
	@NonNullByDefault
	public static class ObservationFactorFunction extends FactorFunction
	{
		public ObservationFactorFunction()
		{
			super("ObservationFactorFunction");
		}
		
		@Override
		public double evalEnergy(Object ... args)
		{
			String state = (String)args[0];
			String observation = (String)args[1];
			double value;
			
			if (state.equals("sunny"))
			{
				if (observation.equals("walk"))
					value = 0.7;
				else if (observation.equals("book"))
					value = 0.1;
				else // cook
					value = 0.2;
			}
			else
			{
				if (observation.equals("walk"))
					value = 0.2;
				else if (observation.equals("book"))
					value = 0.4;
				else // cook
					value = 0.4;
			}
			
			return -Math.log(value);
		}
	}
	
	public static void main(String[] args)
	{
		FactorGraph HMM = new FactorGraph();
		
		DiscreteDomain domain = DiscreteDomain.create("sunny","rainy");
		Discrete MondayWeather = new Discrete(domain);
		Discrete TuesdayWeather = new Discrete(domain);
		Discrete WednesdayWeather = new Discrete(domain);
		Discrete ThursdayWeather = new Discrete(domain);
		Discrete FridayWeather = new Discrete(domain);
		Discrete SaturdayWeather = new Discrete(domain);
		Discrete SundayWeather = new Discrete(domain);

		TransitionFactorFunction trans = new TransitionFactorFunction();
		HMM.addFactor(trans, MondayWeather,TuesdayWeather);
		HMM.addFactor(trans, TuesdayWeather,WednesdayWeather);
		HMM.addFactor(trans, WednesdayWeather,ThursdayWeather);
		HMM.addFactor(trans, ThursdayWeather,FridayWeather);
		HMM.addFactor(trans, FridayWeather,SaturdayWeather);
		HMM.addFactor(trans, FridayWeather,SundayWeather);
		
		ObservationFactorFunction obs = new ObservationFactorFunction();
		HMM.addFactor(obs,MondayWeather,"walk");
		HMM.addFactor(obs,TuesdayWeather,"walk");
		HMM.addFactor(obs,WednesdayWeather,"cook");
		HMM.addFactor(obs,ThursdayWeather,"walk");
		HMM.addFactor(obs,FridayWeather,"cook");
		HMM.addFactor(obs,SaturdayWeather,"book");
		HMM.addFactor(obs,SundayWeather,"book");
		
		
		MondayWeather.setInput(0.7,0.3);
		
		ISolverFactorGraph solver = requireNonNull(HMM.getSolver());
		solver.setNumIterations(20);
		HMM.solve();
		
		double [] belief = TuesdayWeather.getBelief();
		System.out.println(Arrays.toString(belief));
		solver.iterate();
		solver.iterate(5);
	}

}
