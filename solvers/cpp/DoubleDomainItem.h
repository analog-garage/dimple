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
