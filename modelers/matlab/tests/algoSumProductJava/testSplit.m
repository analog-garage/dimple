%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices, Inc.
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

function testSplit()

    a = Bit();
    a.Name = 'a';
    b = Bit();
    b.Name = 'b';

    fg = FactorGraph();

    f = fg.addFactor(@(x,y) x~=y,a,b);
    f.Name = 'unequal';

    b2 = fg.split(b);
    b2.Name = 'b2';
    a2 = fg.split(a,f);
    a2.Name = 'a2';

    %Check that a and a2 are connected by an equals factor
    assertTrue(a.Factors{1} == a2.Factors{1});
    table = a.Factors{1}.FactorTable;
    indices = table.Indices;
    values = table.Weights;
    assertTrue(all(indices(:,1) == indices(:,2)));
    assertTrue(all(values == ones(2,1)));

    %Check that b and b2 are connected by an equals factor
    assertTrue(b.Factors{2} == b2.Factors{1});
    table = b2.Factors{1}.FactorTable;
    indices = table.Indices;
    values = table.Weights;
    assertTrue(all(indices(:,1) == indices(:,2)));
    assertTrue(all(values == ones(2,1)));

    %Check that b2 is not connected to f (only connected to one factor)
    assertTrue(length(b2.Factors) == 1);
    assertTrue(b2.Factors{1} ~= f);

    %Check that b is connected to f and to equals factor
    assertTrue(b.Factors{1} == f);
    assertTrue(b.Factors{2} == b2.Factors{1});

    %Check that a is not connected to f (only connected to one factor)
    assertTrue(length(a.Factors) == 1);
    assertTrue(a.Factors{1} ~= f);

    %Check that a2 is connected to f and equals node
    assertTrue(a2.Factors{2} == f);
    assertTrue(a2.Factors{1} == a2.Factors{1});

    %fg.plot(1);
end
