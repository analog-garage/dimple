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

function testAddBoundaryVariables

    ng = FactorGraph();
    a = Bit();
    b = Bit();
    ng.addBoundaryVariables(a,b);
    ng.addFactor(@xorDelta,a,b);

    fg = FactorGraph();
    a = Bit();
    b = Bit();
    fg.addFactor(ng,a,b);

    a.Input = 0.8;
    fg.solve();
    assertEqual(b.Belief,0.8);
    
    % 
    ng = FactorGraph();
    a = Bit(2,1);
    y = a(1) + a(2);
    ng.addBoundaryVariables(y,a);

    fg = FactorGraph(); 
    y = Discrete(0:2);
    a = Bit(2,1);
    fg.addFactor(ng,y,a);
    a.Input = [1 0];
    fg.solve();

    assertEqual(y.Value,1);
    assertElementsAlmostEqual(y.Belief, [0 1 0]')
    
end
