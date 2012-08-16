/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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

/*
 *  MatrixDomainItem.cpp
 *  DimpleXCode
 *
 *  Created by Shawn Hershey on 5/11/09.
 *  Copyright 2009 __MyCompanyName__. All rights reserved.
 *
 */

#include "MatrixDomainItem.h"
#include "mex.h"

MatrixDomainItem::MatrixDomainItem(vector<double> & realPart, vector<double> & imagPart, vector<int> & dims) : 
		_realPart(realPart), _imagPart(imagPart), _dims(dims)
{
			
}
bool MatrixDomainItem::Equals(IDomainItem * otherItem)
{
	MatrixDomainItem * mOther = (MatrixDomainItem*)otherItem;
	if (_dims.size() != mOther->_dims.size())
	{
		return false;
	}
	for (int i = 0; i < _dims.size(); i++)
	{
		if (_dims[i] != mOther->_dims[i])
		{
			return false;
		}
	}
			
	for (int i = 0; i < _realPart.size(); i++)
	{
		if (_realPart[i] != mOther->_realPart[i] || 
			_imagPart[i] != mOther->_imagPart[i])
		{
			return false;
		}
	}
			
	return true;
}
vector<double> * MatrixDomainItem::GetRealPart()
{
	return & _realPart;
}
vector<double> * MatrixDomainItem::GetImagPart()
{
	return & _imagPart;
}
vector<int> * MatrixDomainItem::GetDims()
{
	return & _dims;
}
