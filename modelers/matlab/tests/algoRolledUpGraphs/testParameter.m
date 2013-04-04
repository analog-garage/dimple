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

function testParameter
    
    v = Bit();
    ng = FactorGraph(v);
    ng.addFactor(@constFactor,v,[.6 .4]);

    fg = FactorGraph();
    v = Bit();
    v.Input = .4;
    fg.addRepeatedFactor(ng,v);

    fg.initialize();

    fg2 = FactorGraph();
    b = Bit();
    b.Input = .4;
    fg.NumSteps = 0;
    for i = 1:10
        fg2.addFactor(@constFactor,b,[.6 .4]);
        fg2.solve();
        fg.solveOneStep();

        assertElementsAlmostEqual(b.Belief,v.Belief);

        fg.advance();
    end
    
    assertEqual(1,length(fg.Variables));
    assertEqual(2,length(fg.Factors));
    
    %Now test that we can have multiple factors connected to a parameter
    %for each step
    a = Bit();
    b = Bit();
    c = Bit();
    ng = FactorGraph(a,b,c);
    ng.addFactor(@xorDelta,a,b);
    ng.addFactor(@xorDelta,a,c);
    
    a = Bit();
    b = BitStream();
    c = BitStream();
    fg = FactorGraph();
    fg.addFactor(ng,a,b,c);
    b.DataSource = DoubleArrayDataSource(repmat([.8 .2]',1,2));
    c.DataSource = DoubleArrayDataSource(repmat([.8 .2]',1,2));
    fg.solve();
    expectedBelief = .2^4 / (.8^4 + .2^4);
    assertElementsAlmostEqual(a.Belief,expectedBelief);
end
