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
			@SuppressWarnings("unchecked")
			T clone = (T) in.readObject();
			return clone;
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
