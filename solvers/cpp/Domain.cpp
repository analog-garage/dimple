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
