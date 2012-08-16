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

/*
 *  MatrixDomainItem.h
 *  DimpleXCode
 *
 *  Created by Shawn Hershey on 5/11/09.
 *  Copyright 2009 __MyCompanyName__. All rights reserved.
 *
 */
/*
 *  MatrixDomainItem.h
 *  DimpleXCode
 *
 *  Created by Shawn Hershey on 5/11/09.
 *  Copyright 2009 __MyCompanyName__. All rights reserved.
 *
 */

#pragma once

#include "IDomainItem.h"
#include <vector>
#include "mex.h"

using namespace std;

class MatrixDomainItem : public IDomainItem
{
public:
	MatrixDomainItem(vector<double> & realPart, vector<double> & imagPart, vector<int> & dims);
	virtual bool Equals(IDomainItem * otherItem);
	vector<double> * GetRealPart();
	vector<double> * GetImagPart();
	vector<int> * GetDims();
private:
	vector<double> _realPart;
	vector<double> _imagPart;
	vector<int> _dims;
};

