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
