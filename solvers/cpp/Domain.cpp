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

#include "stdafx.h"
#include "Domain.h"

Domain::Domain(void)
{

}

Domain::~Domain(void)
{
	for (vector<IDomainItem*>::iterator it = _items.begin(); 
		it != _items.end(); it++)
	{
		delete (*it);
	}
}

void Domain::Add(IDomainItem * object)
{
	_items.push_back(object);
}
IDomainItem * Domain::Get(int item)
{
	return _items[item];
}
unsigned int Domain::Length()
{
	return _items.size();
}

bool Domain::Equals(Domain * other)
{
	if (other->Length() != Length())
		return false;
	else
	{
		for (int i = 0; i < Length(); i++)
		{
			if (!other->Get(i)->Equals(Get(i)))
				return false;
		}
	}
	return true;
}
