#pragma once
#include <vector>
#include "IDomainItem.h"
using namespace std;

class Domain
{
public:
	Domain(void);
	~Domain(void);
	void Add(IDomainItem * item);
	IDomainItem * Get(int item);
	unsigned int Length();
	bool Equals(Domain * domain);
private:
	vector<IDomainItem *> _items;
};
