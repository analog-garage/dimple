package com.analog.lyric.dimple.parameters;

import java.io.Serializable;

/**
 * @param <Key> is the type of keys to the parameter list instance.
 */

public interface IParameterList<Key extends IParameterKey>
	extends Serializable, Iterable<ParameterValue<Key>>, Cloneable
{
	public IParameterList<Key> clone();
	
	public boolean isFixed(Key key);
	public boolean isFixed(int index);
	
	/**
	 * Returns current value associated with given parameter {@code key}.
	 */
	public double get(Key key);
	
	/**
	 * Returns current value associated with given parameter index.
	 */
	public double get(int index);
	
	public Key[] getKeys();
	
	/**
	 * Returns a copy of the parameter values in index order.
	 */
	public double[] getValues();
	
	public Class<Key> keyType();
	
	public void set(Key key, double value);
	
	public void set(int index, double value);
	
	public void setAll(ParameterValue<Key> ... values);
	
	public void setAll(double ...values);
	
	public void setAllToDefault();
	public void setAllMissing();
	
	public void setFixed(int index, boolean fixed);
	public void setFixed(Key key, boolean fixed);

	public int size();
}

