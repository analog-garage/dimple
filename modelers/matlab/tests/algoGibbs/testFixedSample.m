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

function testFixedSample()

    b = Real();
    c = Real();
    fg = FactorGraph();
    fg.Solver = 'gibbs';
    a = b+c;
    b.FixedValue = 2;
    c.FixedValue = 3;
    assertEqual(a.Solver.getCurrentSample(),5);


    b = Real();
    c = Real();
    fg = FactorGraph();
    fg.Solver = 'gibbs';
    b.FixedValue = 2;
    c.FixedValue = 3;
    a = b+c;
    assertEqual(a.Solver.getCurrentSample(),5);
    

    a = Real();
    b = Real();
    fg = FactorGraph();
    fg.Solver = 'gibbs';
    c = a+b;
    a.FixedValue = 2;
    b.FixedValue = 3;
    g = Real();
    g.FixedValue = 3;
    h = Real();
    h.FixedValue = 3;
    d = g+h;
    e = c+d;
    assertEqual(e.Solver.getCurrentSample(),11);
    
    b = Real();
    c = Real();
    fg = FactorGraph();
    fg.Solver = 'gibbs';
    a = b+c;
    b.FixedValue = 2;
    c.FixedValue = 3;
    fg.Solver = 'gibbs';
    assertEqual(a.Solver.getCurrentSample(),5);
    
    a = Real();
    b = Real();
    fg = FactorGraph();
    fg.Solver = 'gibbs';
    c = a+b;
    a.FixedValue = 2;
    b.FixedValue = 3;
    g = Real();
    g.FixedValue = 3;
    h = Real();
    h.FixedValue = 3;
    d = g+h;
    e = c+d;
    fg.Solver = 'gibbs';
    assertEqual(e.Solver.getCurrentSample(),11);
    

    %TODO: test for discrete
end