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

function testLinear()

    fg = FactorGraph();
    fg.Solver = 'Gaussian';

    a = Real();
    b = Real();
    c = Real();
    consts = [1 2 3];
    total = 6;
    fg.addFactor(@linear,a,b,c,consts,total);
    a.Input = [4 1];
    b.Input = [10 1];
    c.Input = [12 100];

    fg.solve();


    fg2 = FactorGraph();
    fg2.Solver = 'Gaussian';
    a2 = Real();
    b2 = Real();
    c2 = Real();

    a2m = Real();
    b2m = Real();
    c2m = Real();
    d = Real();
    d.Input = [6 1e-9];
    fg2.addFactor(@constmult,a2m,a2,consts(1));
    fg2.addFactor(@constmult,b2m,b2,consts(2));
    fg2.addFactor(@constmult,c2m,c2,consts(3));
    a2.Input = a.Input;
    b2.Input = b.Input;
    c2.Input = c.Input;
    fg2.addFactor(@add,d,a2m,b2m,c2m);
    fg2.solve();

    assertElementsAlmostEqual(a.Belief,a2.Belief);
    assertElementsAlmostEqual(b.Belief,b2.Belief);
    assertElementsAlmostEqual(c.Belief,c2.Belief);
end

