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

function testTemplate()

    %Test Factor and Belief
    fg = FactorGraph();
    fg.Solver = 'template';
    d = Discrete([0 1],3,1);
    fg.addFactor(@xorDelta,d);
    d1Input = [3 4];
    d2Input = [6 7];
    d3Input = [10 20];
    d(1).Input = d1Input;
    d(2).Input = d2Input;
    d(3).Input = d3Input;
    fg.solve();
    assertTrue(all(d(3).Belief == (d1Input + d2Input + d3Input)'));

    %Test variable
    fg = FactorGraph();
    fg.Solver = 'template';
    outside = Discrete([0 1],3,1);
    inside = Discrete([0 1]);
    for i = 1:3
        fg.addFactor(@xorDelta,outside(i),inside);
    end
    outside(1).Input = d1Input;
    outside(2).Input = d2Input;
    outside(3).Input = d3Input;
    inInput = [40 50];
    inside.Input = inInput;
    fg.solve();
    assertTrue(all(outside(3).Belief() == (d1Input + d2Input + d3Input + inInput)'));

    %test rolled up graphs
    a = Bit();
    b = Bit();
    ng = FactorGraph(a,b);
    ng.addFactor(@xorDelta,a,b);
    b = BitStream();
    fg = FactorGraph();
    fg.Solver = 'template';
%    fg.Solver = 'sumproduct';
    fs = fg.addFactor(ng,b,b.getSlice(2));
    fs.BufferSize = 1;
    input = [0.7 0.3];
    b.get(1).Input = input;
    fg.initialize();
    fg.solveOneStep();
    fg.advance();
    fg.solveOneStep();
    assertTrue(all(b.Variables(2).Belief==input'));
   
    
end