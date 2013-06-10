package com.analog.lyric.util.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializationTester
{
	/**
	 * Clones {@code object} by serializing and deserializing.
	 * <p>
	 * Intended more for testing serialization rather than deep cloning, although you could use it for that.
	 * <p>
	 * Same idea as {@code SerializationUtils.clone} method from Apache Commons Lang library.
	 */
	public static <T extends Serializable> T clone(T object)
	{
		try
		{
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			out.writeObject(object);
			
			ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
			ObjectInputStream in = new ObjectInputStream(bin);
			return (T) in.readObject();
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
		catch (ClassNotFoundException ex)
		{
			throw new RuntimeException(ex);
		}
	}
}
