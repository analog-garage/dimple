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

package com.analog.lyric.dimple.benchmarks.stereoVision;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import com.analog.lyric.benchmarking.utils.doublespace.DoubleSpace;
import com.analog.lyric.dimple.benchmarks.utils.Image;

public class Dataset
{
	private String _name;
	private DoubleSpace _imageL;
	private DoubleSpace _imageR;

	public Dataset(String name) throws IOException
	{
		setName(name);
		URL urlImageL = this.getClass().getResource(String.format("datasets/%s/imageL.png", name));
		URL urlImageR = this.getClass().getResource(String.format("datasets/%s/imageR.png", name));
		setImageL(Image.loadImage(urlImageL));
		setImageR(Image.loadImage(urlImageR));
		if (!Arrays.equals(getImageL().getDimensions(), getImageR().getDimensions()))
		{
			throw new IllegalArgumentException(
					"Left and right images have different sizes");
		}
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		this._name = name;
	}

	public DoubleSpace getImageL()
	{
		return _imageL;
	}

	public void setImageL(DoubleSpace image)
	{
		this._imageL = image;
	}

	public DoubleSpace getImageR()
	{
		return _imageR;
	}

	public void setImageR(DoubleSpace image)
	{
		this._imageR = image;
	}
}
