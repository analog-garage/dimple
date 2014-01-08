package com.analog.lyric.dimple.model.values;

import com.analog.lyric.dimple.model.domains.ObjectDomain;

/**
 * Holder for arbitrary values represented as {@link Object}.
 */
public class ObjectValue extends Value
{
	/*-------
	 * State
	 */
	
	private Object _object;
	
	/*--------------
	 * Construction
	 */

	ObjectValue(Object value)
	{
		_object = value;
	}
	
	ObjectValue()
	{
		this(null);
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public ObjectValue clone()
	{
		return new ObjectValue(this._object);
	}

	@Override
	public ObjectDomain getDomain()
	{
		return ObjectDomain.instance();
	}
	
	@Override
	public Object getObject()
	{
		return _object;
	}

	@Override
	public void setObject(Object value)
	{
		_object = value;
	}
	
}
