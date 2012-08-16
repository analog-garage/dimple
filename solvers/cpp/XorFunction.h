/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

#include "IFunctionPointer.h"
#include "DoubleDomainItem.h"
#include <vector>
using namespace std;

//This class was created to test the C++ code.  This function is not used by the Matlab API
class XorFunction : public IFunctionPointer
{
public:
	XorFunction(void);
	~XorFunction(void);

	virtual double Evaluate(vector<IDomainItem*> & parameters)
	{
		int retVal = 1;
		for (unsigned int i = 0; i < parameters.size(); i++)
		{
			retVal ^= (int)((DoubleDomainItem*)parameters[i])->GetValue();
		}
		return (double)retVal;
	}
};
