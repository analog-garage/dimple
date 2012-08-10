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

