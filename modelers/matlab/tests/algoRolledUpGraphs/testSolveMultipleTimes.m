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

function testSolveMultipleTimes()

    %solve for a while
    b = Bit(2,1);
    ng = FactorGraph(b);
    ng.addFactor(@xorDelta,b);

    fg = FactorGraph();
    b = BitStream();
    fg.addFactor(ng,b,b.getSlice(2));

    b.DataSink = DoubleArrayDataSink();
    b.Variables(1).Input = [.8 .2];

    fg.NumSteps = 10;
    fg.solve();
    fg.solve(false);
    fg.solve(false);
    assertEqual(b.Variables(1).Belief,[.8 .2]);
    fg.solve();
    assertEqual(b.Variables(1).Belief,[.5 .5]);
    %keep solving
end