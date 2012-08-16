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

function testRemoveFactor()

    fg = FactorGraph();
    b = Bit(3,1);
    f1 = fg.addFactor(@xorDelta,b(1:2)); %#ok<NASGU>
    f2 = fg.addFactor(@xorDelta,b(2:3));

    b.Input = [.8 .8 .6];

    fg.Solver.setNumIterations(2);
    fg.solve();

    assertElementsAlmostEqual([.96 .96 .96]',b.Belief);

    fg.removeFactor(f2);

    fg.solve();

    p1 = .8*.8;
    p0 = .2*.2;
    total = p1+p0;
    p1 = p1/total;
    p0 = p0/total; %#ok<NASGU>
    assertElementsAlmostEqual([p1 p1 .6]',b.Belief);
    
end
