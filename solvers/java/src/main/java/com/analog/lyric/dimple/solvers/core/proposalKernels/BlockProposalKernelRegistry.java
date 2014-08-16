/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.core.proposalKernels;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.Nullable;

public class BlockProposalKernelRegistry
{
	private static ArrayList<String> _packages = new ArrayList<String>();
	static
	{
		_packages.add(BlockProposalKernelRegistry.class.getPackage().getName());
	}
	
	// Get a proposal kernel by name; assumes it is located in this package
	public static @Nullable IBlockProposalKernel get(String proposalKernelName)
	{
		for (String s : _packages)
		{
			String fullQualifiedName = s + "." + proposalKernelName;
			try
			{
				IBlockProposalKernel proposalKernel = (IBlockProposalKernel)(Class.forName(fullQualifiedName).getConstructor().newInstance());
				return proposalKernel;
			}
			catch (Exception e)
			{
				continue;
			}
		}
		return null;
	}
	
	
	public static void addPackage(String packageName)
	{
		_packages.add(packageName);
	}
}
