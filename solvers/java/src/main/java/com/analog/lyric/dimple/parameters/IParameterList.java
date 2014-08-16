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

package com.analog.lyric.dimple.parameters;

import java.io.Serializable;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for holding a set of parameters associated with a parametric function.
 * <p>
 * Implementors can subclass one of {@link ParameterList1}, {@link ParameterList2}, ... {@link ParameterListN}
 * appropriate to the number of parameters with implementations of the {@link #clone()} and {@link #getKeys()}
 * methods. If the keys are the instances of an enum type (see example in {@link IParameterKey}), then this
 * {@link #getKeys()} can be implemented simply by returning {@code KeyEnumType.values()}.
 * <p>
 * @param <Key> is the type of keys to the parameter list instance, but is not
 * relevant for parameter lists that do not support keys (for which {@link #hasKeys()} is false).
 */
public interface IParameterList<Key extends IParameterKey>
	extends Serializable, Iterable<Parameter<Key>>, Cloneable
{
	/**
	 * True if individual parameter values may be shared. If false, then
	 * attempting to set shared values will result in an exception.
	 * @see #setSharedValue(int, SharedParameterValue)
	 */
	public boolean canShare();
	
	/**
	 * Returns a copy of this list.
	 * <p>
	 * Implementors should override this method with the return type declared as the type of
	 * the overriding class, and should implement it by calling a copy constructor specific
	 * to the overriding type (as opposed to calling {@code super.clone()}.
	 * <p>
	 * If list has any shared parameters, the new list's {@link #getSharedValue} must return the same value object
	 * as the original list for any shared parameter.
	 */
	public IParameterList<Key> clone();
	
	/**
	 * Indicates whether parameter with given {@code key} is fixed. If fixed, then attempts to
	 * set it will result in an exception.
	 * <p>
	 * @throws UnsupportedOperationException if {@link #hasKeys()} is false.
	 * @see #isFixed(int)
	 * @see #setFixed(IParameterKey, boolean)
	 */
	public boolean isFixed(Key key);
	
	/**
	 * Indicates whether parameter with given index is fixed.If fixed, then attempts to
	 * set it will result in an exception.
	 * <p>
	 * @throws IndexOutOfBoundsException if index is less than zero or greater than or equal to {@link #size()}.
	 * @see #isFixed(IParameterKey)
	 * @see #setFixed(IParameterKey, boolean)
	 */
	public boolean isFixed(int index);
	
	/**
	 * Indicates whether parameter value may be shared with other parameter lists (i.e. parameter tying).
	 * <p>
	 * @throws UnsupportedOperationException if {@link #hasKeys()} is false.
	 * @see #isShared(int)
	 */
	public boolean isShared(Key key);
	
	/**
	 * Indicates whether parameter value may be shared with other parameter lists (i.e. parameter tying).
	 * <p>
	 * @throws IndexOutOfBoundsException if index is less than zero or greater than or equal to {@link #size()}.
	 * @see #isShared(int)
	 */
	public boolean isShared(int index);
	
	/**
	 * Returns current value associated with given parameter {@code key}.
	 * <p>
	 * @throws UnsupportedOperationException if {@link #hasKeys()} is false.
	 */
	public double get(Key key);
	
	/**
	 * Returns current value associated with given parameter index.
	 * <p>
	 * @throws IndexOutOfBoundsException if index is less than zero or greater than or equal to {@link #size()}.
	 */
	public double get(int index);
	
	/**
	 * Returns an array of all of the keys defined for this parameter list, or else
	 * null if the parameter list may only be accessed by index. If non-null, the length
	 * of the array must equal {@link #size()}.
	 * @see #hasKeys()
	 */
	public @Nullable Key[] getKeys();
	
	/**
	 * Returns {@link SharedParameterValue} object containing parameter value if parameter
	 * {@link #isShared(IParameterKey)}; otherwise returns null. If non-null, the value
	 * object may be used to access or modify the parameter value.
	 * <p>
	 * @throws UnsupportedOperationException if {@link #hasKeys()} is false.
	 * @see #getSharedValue(int)
	 */
	public @Nullable SharedParameterValue getSharedValue(Key key);
	
	/**
	 * Returns {@link SharedParameterValue} object containing parameter value if parameter
	 * {@link #isShared(IParameterKey)}; otherwise returns null. If non-null, the value
	 * object may be used to access or modify the parameter value.
	 * <p>
	 * @throws IndexOutOfBoundsException if index is less than zero or greater than or equal to {@link #size()}.
	 * @see #getSharedValue(IParameterKey)
	 */
	public @Nullable SharedParameterValue getSharedValue(int index);
	
	/**
	 * True if parameter list supports parameter access by key.
	 * @see #getKeys()
	 */
	public boolean hasKeys();
	
	/**
	 * Returns a copy of the parameter values in index order. The length of the array must equal {@link #size()}.
	 */
	public double[] getValues();

	/**
	 * Sets the parameter with the specified {@code key} to {@code value}.
	 * <p>
	 * @throws UnsupportedOperationException if {@link #hasKeys()} is false.
	 * @see #set(int, double)
	 */
	public void set(Key key, double value);
	
	/**
	 * Sets the parameter with the specified {@code index} to {@code value}.
	 * <p>
	 * @throws UnsupportedOperationException if {@link #hasKeys()} is false.
	 * @see #set(IParameterKey, double)
	 * @see #setAll(Iterable)
	 */
	public void set(int index, double value);
	
	/**
	 * Sets corresponding parameters from specified {@code values}. The values can be in any order and
	 * do not have to be complete.
	 * <p>
	 * @throws IndexOutOfBoundsException if index of a value is negative or not less than {@link #size()}.
	 * @see #setAll(double...)
	 */
	public void setAll(Iterable<Parameter<Key>> values);
	
	/**
	 * Sets corresponding parameters from specified array of {@code values}, where each parameter is
	 * set to the value with the corresponding index.
	 * <p>
	 * @throws IndexOutOfBoundsException if length of {@code values} is greater than {@link #size()}.
	 */
	public void setAll(double ...values);
	
	/**
	 * Sets all non-fixed parameters to their default values, if any. If {@link #hasKeys()} is true, then
	 * this is expected to be the value of {@link IParameterKey#defaultValue()} for the corresponding
	 * key.
	 */
	public void setAllToDefault();
	
	/**
	 * Sets all non-fixed parameters to NaN to indicate value is missing.
	 */
	public void setAllMissing();
	
	/**
	 * Sets all parameter's fixed attribute.
	 * 
	 * @see #setFixed(int, boolean)
	 */
	public void setAllFixed(boolean fixed);
	
	/**
	 * Sets whether parameter with given index should be considered fixed.
	 * @throws IndexOutOfBoundsException if index of a value is negative or not less than {@link #size()}.
	 * @see #isFixed(int)
	 * @see #setFixed(IParameterKey, boolean)
	 */
	public void setFixed(int index, boolean fixed);
	
	/**
	 * Sets whether parameter with given index should be considered fixed.
	 * <p>
	 * @throws UnsupportedOperationException if {@link #hasKeys()} is false.
	 * @see #isFixed(IParameterKey)
	 * @see #setFixed(int, boolean)
	 * */
	public void setFixed(Key key, boolean fixed);

	public void setSharedValue(Key key, @Nullable SharedParameterValue value);
	
	public void setSharedValue(int index, @Nullable SharedParameterValue value);
	
	/**
	 * Sets whether given parameter may be shared.
	 * 
	 * @throws UnsupportedOperationException if {@link #hasKeys()} is false or implementation
	 * does not support parameter sharing.
	 * @see #canShare()
	 * @see #setShared(int, boolean)
	 * @see #isShared(IParameterKey)
	 */
	public void setShared(Key key, boolean shared);
	
	/**
	 * Sets whether given parameter may be shared.
	 * 
	 * @throws UnsupportedOperationException if implementation does not support parameter sharing.
	 * @throws IndexOutOfBoundsException if index of a value is negative or not less than {@link #size()}.
	 * @see #canShare()
	 * @see #setShared(int, boolean)
	 * @see #isShared(IParameterKey)
	 */
	public void setShared(int index, boolean shared);
	
	/**
	 * The number of parameters in the list, which specifies the valid range
	 * of parameter indexes: [0, size-1]. The size is constant.
	 */
	public int size();
}

