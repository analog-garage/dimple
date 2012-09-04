/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.util.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TrivialMD5Sum
{
	static public MessageDigest getMessageDigest(byte[] buffer) throws NoSuchAlgorithmException
	{
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(buffer, 0, buffer.length);
		return digest;
	}
	static public byte[] getDigestBytes(byte[] buffer) throws NoSuchAlgorithmException
	{
		MessageDigest messageDigest = getMessageDigest(buffer);
		return messageDigest.digest();
	}
	static public BigInteger getDigestBigInteger(byte[] buffer) throws NoSuchAlgorithmException
	{
		return new BigInteger(1, getDigestBytes(buffer));
	}

	static public MessageDigest getMessageDigest(File f) throws NoSuchAlgorithmException, FileNotFoundException
	{
		MessageDigest digest = MessageDigest.getInstance("MD5");
		InputStream is = new FileInputStream(f);
		byte[] buffer = new byte[8192];
		int read = 0;
		try {
			while( (read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}
		}
		catch(IOException e) {
			throw new RuntimeException("Unable to process file for MD5", e);
		}
		finally {
			try {
				is.close();
			}
			catch(IOException e) {
				throw new RuntimeException("Unable to close input stream for MD5 calculation", e);
			}
		}
		return digest;
	}
	static public byte[] getDigestBytes(File f) throws NoSuchAlgorithmException, FileNotFoundException
	{
		MessageDigest messageDigest = getMessageDigest(f);
		return messageDigest.digest();
	}
	static public BigInteger getDigestBigInteger(File f) throws NoSuchAlgorithmException, FileNotFoundException
	{
		return new BigInteger(1, getDigestBytes(f));
	}
	
	public static String getMD5StringFromBytes(byte[] b)
	{
		String result = "";

		for (int i=0; i < b.length; i++) {
			result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		}
		return result;
	}

}
