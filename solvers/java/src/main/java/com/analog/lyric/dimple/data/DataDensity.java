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

package com.analog.lyric.dimple.data;

/**
 * Specifies density of representation for {@link DataLayer} classes.
 * <p>
 * Used in constructors of {@link DataLayer} and {@link FactorGraphData} classes.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public enum DataDensity
{
	/**
	 * Specifies a sparse hash-table based representation.
	 * <p>
	 * This holds all data values in a hash table for each factor graph. Getting/setting is constant time
	 * but slower than the DENSE representation. It requires O(V) memory where V are the number of data
	 * values actually stored in the layer.
	 */
	SPARSE,
	
	/**
	 * Specifies a dense array-based representation.
	 * <p>
	 * This holds all data values in an array for each factor graph, so getting/setting only costs
	 * a couple of array accesses. It requires O(K) memory where K is the number of keys.
	 */
	DENSE;
}
