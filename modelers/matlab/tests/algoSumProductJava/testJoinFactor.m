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

function testJoinFactor()

    %input = rand(4,1);
    input = [0.6892 0.7482 0.4505 0.0838];


    %%%%%%%%%%
    b = Bit(4,1);
    fg = FactorGraph();
    fg.addFactor(@(a,b,c,d) (mod(a+b+c,2)==0) && (mod(b+c+d,2) == 0),b(1),b(2),b(3),b(4));

    b.Input = input;
    fg.solve();
    expectedBelief = b.Belief;



    %Test common variables
    b = Bit(4,1);
    fg = FactorGraph();
    f1 = fg.addFactor(@xorDelta,b(1:3));
    f2 = fg.addFactor(@xorDelta,b(2:4));

    f3 = fg.join(f1,f2);

    b.Input = input;
    
    
    fg.solve();
    actualBelief = b.Belief;

    assertElementsAlmostEqual(actualBelief,expectedBelief);
    assertTrue(length(fg.Factors)==1);
    assertTrue(fg.Factors{1} == f3);

    %Test no common variables
    b = Bit(6,1);
    fg = FactorGraph();
    f1 = fg.addFactor(@xorDelta,b(1:3));
    f2 = fg.addFactor(@xorDelta,b(4:6));

    b.Input = rand(6,1);

    fg.solve();

    expectedBelief = b.Belief;

    f3 = fg.join(f1,f2);

    fg.solve();
    actualBelief = b.Belief;

    assertElementsAlmostEqual(expectedBelief,actualBelief);
    assertTrue(length(fg.Factors)==1);
    assertTrue(fg.Factors{1} == f3);
    assertEqual(size(f3.FactorTable.Indices),[4*4 3+3]);



    %Test multiplication of factors
    b = Bit(2,1);
    fg = FactorGraph();
    f1 = fg.addFactor(@(x,y) x+y,b(1),b(2));
    f2 = fg.addFactor(@(x,y) (x+2)*(y+1),b(1),b(2));

    f3 = fg.join(f1,f2);
    assertEqual(double(f3.FactorTable.Indices),[1 0; 0 1; 1 1]);
    assertEqual(f3.FactorTable.Weights,[3 4 12]');
   
    %Test mulitple factors
    b = Bit(4,1);
    fg = FactorGraph();
    f1 = fg.addFactor(@xorDelta,b(1:2));
    f2 = fg.addFactor(@xorDelta,b(2:3));
    f3 = fg.addFactor(@xorDelta,b(3:4));
    f4 = fg.join(f1,f2,f3);
    
    assertEqual(double(f4.FactorTable.Indices),[0 0 0 0; 1 1 1 1]);
    assertTrue(length(fg.Factors)==1);
    assertTrue(fg.Factors{1} == f4);

end

