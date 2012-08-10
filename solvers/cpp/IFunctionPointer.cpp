#include "stdafx.h"
#include "IFunctionPointer.h"

IFunctionPointer::IFunctionPointer() : _hashValue(IFunctionPointer::_currentHashValue)
{
	IFunctionPointer::_currentHashValue++;
}

int IFunctionPointer::GetHashValue()
{
	return _hashValue;
}

int IFunctionPointer::_currentHashValue = 0;
