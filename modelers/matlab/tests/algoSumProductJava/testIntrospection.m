%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
%
%   Licensed under the Apache License, Version 2.0 (the "License");
%   you may not use this file except in compliance with the License.
%   You may obtain a copy of the License at
%
%       http://www.apache.org/licenses/LICENSE-2.0
%
%   Unless required by applicable law or agreed to in writing, software
%   distributed under the License is distributed on an "AS IS" BASIS,
%   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%   See the License for the specific language governing permissions and
%   limitations under the License.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function testIntrospection


    fg = FactorGraph();
    b = Bit(5,1);
    f1 = fg.addFactor(@xorDelta,b(1:3));
    f2 = fg.addFactor(@xorDelta,b(3:5));
    ct1 = f1.FactorTable;
    ct2 = f2.FactorTable;

    %Make sure combo tables are the same
    assertEqual(ct1.Indices,ct2.Indices);
    assertEqual(ct1.Weights,ct2.Weights);

    %compare against golden
    expectedTable = [0 0 0; 1 1 0; 1 0 1; 0 1 1];
    expectedValues = [1 1 1 1]';

    assertEqual(expectedTable,double(ct1.Indices));
    assertEqual(expectedValues,ct1.Weights);

    %Now lets make sure we get the right variables from factors
    %and the other way around.  
    f1vars = f1.Variables;    

    for i = 1:3
        assertTrue(b(i)==f1vars{i});
    end

    f2vars = f2.Variables;

    for i = 1:3
        assertTrue(b(i+2)==f2vars{i});
    end


    b1facs = b(1).Factors;
    b3facs = b(3).Factors;

    assertTrue(b1facs{1} == f1);
    assertTrue(b3facs{1} == f1);
    assertTrue(b3facs{2} == f2);

    factors = fg.Factors;
    assertTrue(factors{1} == f1);
    assertTrue(factors{2} == f2);

    assertEqual(findstr(f1.Label,'xorDelta'),1);
    assertEqual(findstr(f2.Label,'xorDelta'),1);

    vars = fg.Variables;
    for i = 1:numel(vars)
       assertTrue(vars{i}==b(i));
    end
        
    assertEqual(double(b1facs{1}.FactorTable.Indices),[0 0 0; 1 1 0; 1 0 1; 0 1 1]);
    
    f = fg.Factors{1};
    assertEqual(double(f.FactorTable.Indices),[0 0 0; 1 1 0; 1 0 1; 0 1 1]);
    
    assertEqual([1 1 1 1]',b.Factors{1}.FactorTable.Weights);
    b.Factors{1}.FactorTable.Weights = [1 2 3 4];
    assertEqual([1 2 3 4]',b.Factors{1}.FactorTable.Weights);
end
