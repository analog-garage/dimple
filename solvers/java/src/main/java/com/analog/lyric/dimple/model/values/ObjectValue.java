package com.analog.lyric.dimple.model.values;

public class ObjectValue extends Value
{
	/*-------
	 * State
	 */
	
	private Object _object;
	
	/*--------------
	 * Construction
	 */

	public ObjectValue(Object value)
	{
		_object = value;
	}
	
	public ObjectValue()
	{
		this(null);
	}
	
	public ObjectValue(ObjectValue that)
	{
		this(that._object);
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public ObjectValue clone()
	{
		return new ObjectValue(this);
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
