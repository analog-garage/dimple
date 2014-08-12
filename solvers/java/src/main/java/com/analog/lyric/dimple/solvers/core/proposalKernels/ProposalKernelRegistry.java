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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import com.analog.lyric.util.misc.Nullable;


// TODO: rename ProposalKernelFactory?
// TODO: should we have an instance per DimpleEnvironment?
// TODO: should there be a way to iterate over contents?

@ThreadSafe
public enum ProposalKernelRegistry
{
	INSTANCE;
	
	@GuardedBy("this")
	private final ArrayList<String> _packages;
	@GuardedBy("this")
	private final Map<String, Constructor<IProposalKernel>> _nameToConstructor;
	
	private ProposalKernelRegistry()
	{
		_packages = new ArrayList<String>();
		_packages.add(getClass().getPackage().getName());
		
		_nameToConstructor = new ConcurrentHashMap<>();
	}
	
	private synchronized @Nullable Constructor<IProposalKernel> getConstructor(String proposalKernelName)
	{
		Constructor<IProposalKernel> constructor = INSTANCE._nameToConstructor.get(proposalKernelName);
		
		if (constructor == null)
		{
			for (String packageName : _packages)
			{
				String fullQualifiedName = packageName + "." + proposalKernelName;
				try
				{
					@SuppressWarnings("unchecked")
					Class<IProposalKernel> kernelClass = (Class<IProposalKernel>)Class.forName(fullQualifiedName);
					if (IProposalKernel.class.isAssignableFrom(kernelClass))
					{
						constructor = (kernelClass).getConstructor();
						_nameToConstructor.put(proposalKernelName, constructor);
						break;
					}
				}
				catch (Exception e)
				{
				}
			}
		}
		
		return constructor;
	}
	
	public static @Nullable Class<? extends IProposalKernel> getClass(String proposalKernelName)
	{
		Constructor<IProposalKernel> constructor = INSTANCE.getConstructor(proposalKernelName);
		return constructor != null ? constructor.getDeclaringClass() : null;
	}
	
	// Get a proposal kernel by name; assumes it is located in this package
	public static @Nullable IProposalKernel get(String proposalKernelName)
	{
		Constructor<IProposalKernel> constructor = INSTANCE.getConstructor(proposalKernelName);
		if (constructor != null)
		{
			try
			{
				return constructor.newInstance();
			}
			catch (Exception ex)
			{
				// Ignore
			}
		}
		
		return null;
	}
	
	public static void addPackage(String packageName)
	{
		synchronized(INSTANCE)
		{
			INSTANCE._packages.add(packageName);
		}
	}
}
