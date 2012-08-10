#pragma once

class IDomainItem
{
public:
	virtual bool Equals(IDomainItem * otherItem) = 0;
};
