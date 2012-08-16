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

function testOrderBug()

    b = Bit(4,1);
    fourXor = FactorGraph(b);
    fourXor.addFactor(@xorDelta,b(1:3));
    fourXor.addFactor(@xorDelta,b(2:4));

    b = Bit(6,1);
    fg = FactorGraph(b);
    g = fg.addFactor(fourXor,b(1:4));
    fg.addFactor(fourXor,b(3:6));

    fg2 = FactorGraph();
    b = Bit(8,1);
    g = fg2.addFactor(fg,b(1:6));
    fg2.addFactor(fg,b(3:8));

    b.Input = ones(8,1)*.8;
    fg2.Solver.setNumIterations(100);
    fg2.solve();

    
    expected = [1 1 0 1 1 0 1 1]';
    assertEqual(expected,b.Value);
end
