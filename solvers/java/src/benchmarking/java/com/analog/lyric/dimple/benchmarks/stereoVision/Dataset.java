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

public class Dataset
{
	private String _name;
	private Image _imageL;
	private Image _imageR;

	public Dataset(String name) throws IOException
	{
		setName(name);
		URL urlImageL = this.getClass().getResource(String.format("datasets/%s/imageL.png", name));
		URL urlImageR = this.getClass().getResource(String.format("datasets/%s/imageR.png", name));
		setImageL(Image.load(urlImageL));
		setImageR(Image.load(urlImageR));
		if (getImageL().getWidth() != getImageR().getWidth()
				|| getImageL().getHeight() != getImageR().getHeight())
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

	public Image getImageL()
	{
		return _imageL;
	}

	public void setImageL(Image imageL)
	{
		this._imageL = imageL;
	}

	public Image getImageR()
	{
		return _imageR;
	}

	public void setImageR(Image imageR)
	{
		this._imageR = imageR;
	}
}
