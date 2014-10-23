/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.jsproxy;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.factors.Factor;

/**
 * Javascript API representation of a Dimple factor.
 * <p>
 * This delegates to an underlying Dimple {@link Factor} object.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class JSFactor extends JSNode<Factor>
{
	JSFactor(JSFactorGraph parent, Factor factor)
	{
		super(parent, factor);
	}
	
	/*---------------------
	 * JSProxyNode methods
	 */
	
	/**
	 * The underlying factor function that specifies how the factor is evaluatged.
	 * @since 0.07
	 */
	public JSFactorFunction getFactorFunction()
	{
		DimpleApplet applet = getApplet();
		JSFactorFunctionFactory functions = applet != null ? applet.functions : new JSFactorFunctionFactory();
		return functions.wrap(_delegate.getFactorFunction());
	}
	
	@Override
	public JSNode.Type getNodeType()
	{
		return JSNode.Type.FACTOR;
	}
	
	/**
	 * Returns index of output variable or -1 if there is not exactly one directed output.
	 * @since 0.07
	 */
	public int getOutputIndex()
	{
		int[] indices = _delegate.getDirectedTo();
		return indices != null && indices.length == 1 ? indices[0] : -1;
	}
	
	/**
	 * Returns indices of directed output variables, or null if not directed.
	 * @since 0.07
	 */
	public @Nullable int[] getOutputIndices()
	{
		return _delegate.getDirectedTo();
	}
	
	public boolean isDirected()
	{
		return _delegate.isDirected();
	}
}
