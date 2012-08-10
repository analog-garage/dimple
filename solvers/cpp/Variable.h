#pragma once

#include <string>
#include <vector>
#include "Domain.h"
#include "Port.h"
#include "INode.h"

using namespace std;

class Variable : public INode
{
public:
	Variable(int id,int domainLength);
	~Variable(void);

	int GetId();
    int GetDomainLength();
	void SetPriors(vector<double> & priors);
	vector<double> * GetPriors();
	void Initialize();
	//void Update(int portNum);
	void Update();	
	void Connect(Port * port);
	void GetBeliefs(vector<double> & outBeliefs);
private:
    int _domainLength;
	vector<double> _priors;
	vector<Port*> _ports;
	int _numPriors;
	int _id;
};
