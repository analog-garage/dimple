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

package com.analog.lyric.dimple.model;

public class Bit extends Discrete 
{

	public Bit()  
	{
		super(new DiscreteDomain(new Object[]{new Integer(1),new Integer(0)}));
	}
	
	public double getP1()
	{
		return getBelief()[0];
	}

	public void setInput(double p1)
	{
		setInput(p1,1-p1);
	}
	
}
