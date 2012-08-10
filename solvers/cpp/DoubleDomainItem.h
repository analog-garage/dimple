#pragma once

#include "IDomainItem.h"

class DoubleDomainItem : public IDomainItem
{
public:
	DoubleDomainItem(double value) : _value(value)
	{
	}
	virtual bool Equals(IDomainItem * otherItem)
	{
		return _value == ((DoubleDomainItem*)otherItem)->_value;
	}
	double GetValue()
	{
		return _value;
	}
private:
	double _value;
};
