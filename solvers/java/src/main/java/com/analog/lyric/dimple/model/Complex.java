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

public class Complex extends RealJoint 
{
	// Constructors...
	public Complex() 
	{
		this(RealJointDomain.create(2));
	}
	public Complex(RealJointDomain domain)
	{
		this(domain, "Complex");
	}
	public Complex(RealJointDomain domain, String modelerClassName) 
	{
		super(domain, modelerClassName);
		
		if (domain.getNumVars() != 2)
			throw new DimpleException("Complex domain must have exactly two components");
	}
}
