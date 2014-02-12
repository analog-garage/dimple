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
function testGeneralFactor()


    fg = FactorGraph();
    fg.Solver = 'Gaussian';
    fg.Solver.setNumSamples(10000);
    fg.Solver.setSeed(1);

    %%%%%%%%%%%%%%%%%%%%%%%%
    % Test 1
    a = Real();
    b = Real([0 Inf]);
    fg.addFactor('Square',a,b);

    a.Input = [401 40.02];
    b.Input = [0 Inf];

    fg.solve();

    expectedBelief = [20; 1];
    diff = abs(b.Belief-expectedBelief);
    fracDiff = diff./expectedBelief;
    assertTrue(all(fracDiff < .02));


end
