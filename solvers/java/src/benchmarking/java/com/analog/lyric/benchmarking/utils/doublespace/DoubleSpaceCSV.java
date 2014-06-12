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

package com.analog.lyric.benchmarking.utils.doublespace;

import static java.util.Objects.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.analog.lyric.util.misc.Nullable;

public class DoubleSpaceCSV
{

	/**
	 * Constructs a 2-dimensional DoubleSpace populated with data read from a
	 * CSV input stream.
	 * 
	 * @param is
	 *            The input stream from which CSV data is read.
	 * @return The constructed 2-dimensional DoubleSpace.
	 * @throws IOException
	 */
	public static @Nullable DoubleSpace loadCSV(InputStream is) throws IOException
	{
		final BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		final ArrayList<String> lines = new ArrayList<String>();
		while ((line = br.readLine()) != null)
		{
			lines.add(line);
		}
		final int rows = lines.size();
		int columns = -1;
		DoubleSpace result = null;
		if (rows > 0)
		{
			int row = 0;
			for (final String line2 : lines)
			{
				final String[] tokens = line2.split(",");
				if (columns == -1)
				{
					columns = tokens.length;
					result = DoubleSpaceFactory.create(rows, columns);
				}
				else if (columns != tokens.length)
				{
					throw new IllegalArgumentException("All rows of csv text must have same quantity of columns.");
				}
				requireNonNull(result);
				for (int column = 0; column < columns; column++)
				{
					final double value = Double.parseDouble(tokens[column]);
					result.put(value, row, column);
				}
				row += 1;
			}
		}
		return result;
	}

}
