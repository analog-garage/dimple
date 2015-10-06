/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.model.variables;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.google.common.base.Predicate;

/**
 * Canned predicates for testing variable attributes.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public final class VariablePredicates
{
	// Prevent instantiation
	private VariablePredicates() {}
	
	private static enum IsDiscrete implements Predicate<Variable>
	{
		INSTANCE;
		
		@Override
		public boolean apply(@Nullable Variable var)
		{
			return var instanceof Discrete;
		}
	}
	
	/**
	 * Returns a predicate that is true for {@link Discrete} variables.
	 * @since 0.08
	 */
	static public Predicate<Variable> isDiscrete()
	{
		return IsDiscrete.INSTANCE;
	}
	
	private static enum IsUnboundedReal implements Predicate<Variable>
	{
		INSTANCE;
		
		@Override
		public boolean apply(@Nullable Variable var)
		{
			return var != null && var.getDomain().equals(RealDomain.unbounded());
		}
	}
	
	/**
	 * Returns a predicate that is true for {@link Real} variables with unbounded domain.
	 * @since 0.08
	 */
	static public Predicate<Variable> isUnboundedReal()
	{
		return IsUnboundedReal.INSTANCE;
	}

	private static enum IsUnboundedRealJoint implements Predicate<Variable>
	{
		INSTANCE;
		
		@Override
		public boolean apply(@Nullable Variable var)
		{
			if (var != null)
			{
				Domain domain = var.getDomain();
				return domain.isRealJoint() && !domain.isBounded();
			}
			
			return false;
		}
	}
	
	/**
	 * Returns a predicate that is true for {@link RealJoint} variables with unbounded domain.
	 * @since 0.08
	 */
	static public Predicate<Variable> isUnboundedRealJoint()
	{
		return IsUnboundedRealJoint.INSTANCE;
	}
}
