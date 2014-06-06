/*******************************************************************************
 * Copyright 2014 Analog Devices, Inc. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 ********************************************************************************/

package com.analog.lyric.util.misc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Defines interface for writing to a {@link PrintStream} with different levels of verbosity.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public interface IPrintable
{

	/**
	 * Outputs description of event.
	 * 
	 * @param out is the output print stream that will receive the output.
	 * @param verbosity specifies the verbosity of the output:
	 * <ul>
	 * <li>< 0: no output
	 * <li>0: minimal output
	 * <li>1: normal output
	 * <li>>= 2: verbose output
	 * </ul>
	 * It is unspecified how many levels of verbosity are supported by any given subclass.
	 * @since 0.06
	 * @see #toString(int)
	 */
	public abstract void print(PrintStream out, int verbosity);
	
	/**
	 * Outputs description of event followed by newline.
	 * <p>
	 * If {@code verbosity} is less than zero does nothing, otherwise invokes
	 * {@link #print(PrintStream, int)} and then {@link PrintStream#println()}.
	 * <p>
	 * The static method {@link IPrintable.Methods#println(IPrintable, PrintStream, int)} can be used
	 * to implement this as follows:
	 * 
	 * <pre>
	 * public final void println(PrintStream out, int verbosity)
	 * {
	 *     IPrintable.Methods.println(this, out, verbosity);
	 * }
	 * </pre>
	 * @since 0.06
	 */
	public abstract void println(PrintStream out, int verbosity);

	/**
	 * Returns string describing event.
	 * <p>
	 * Uses {@link #print(PrintStream, int)} method to format the string.
	 * <p>
	 * The static method {@link IPrintable.Methods#toString(IPrintable, int)} can be used to
	 * implement this as follows:
	 * <pre>
	 * public String toString(int verbosity)
	 * {
	 *     return IPrintable.Methods.toString(this, verbosity);
	 * }
	 * </pre>
	 * @since 0.06
	 */
	public String toString(int verbosity);

	/**
	 * Provides default implementations of some {@link IPrintable} methods.
	 * 
	 * @since 0.06
	 */
	public static class Methods
	{
		/**
		 * Outputs printable with newline.
		 * <p>
		 * Invokes {@link IPrintable#print(PrintStream, int)} followed by newline if {@code verbosity}
		 * is non-negative.
		 * <p>
		 * Can be used to implement {@link IPrintable#println(PrintStream, int)}.
		 * <p>
		 * @since 0.06
		 */
		public static void println(IPrintable printable, PrintStream out, int verbosity)
		{
			if (verbosity >= 0)
			{
				printable.print(out, verbosity);
				out.println();
			}
		}
		
		/**
		 * Returns printable formatted as string.
		 * <p>
		 * Uses {@link IPrintable#print(PrintStream, int)} to format string. Returns empty
		 * string if {@code verbosity} is negative.
		 * <p>
		 * Can be used to implement {@link IPrintable#toString(int)}.
		 * <p>
		 * @since 0.06
		 */
		public static String toString(IPrintable printable, int verbosity)
		{
			String result = "";
			if (verbosity >= 0)
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				printable.print(new PrintStream(out), verbosity);
				result = out.toString();
			}
			return result;
		}
	}
}