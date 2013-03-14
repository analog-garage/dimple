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

function testHoldSample()

    repeatable = true;

    if (repeatable)
        seed = 1;
        rs=RandStream('mt19937ar');
        RandStream.setGlobalStream(rs);
        reset(rs,seed);
    end

    fg = FactorGraph();
    fg.Solver = 'gibbs';
    a = Discrete([0 1 2]);
    b = Discrete([0 1 2]);
    b.Input = [0.01 0.98 0.01];
    fg.addFactor(@(a,b) a==b,a,b);
    fg.Solver.setNumSamples(10000);
    fg.Solver.setBurnInUpdates(100);
    fg.Solver.setNumRestarts(0);

    fg.solve();

    assertElementsAlmostEqual(a.Solver.getBestSample(),1);
    assertElementsAlmostEqual(b.Solver.getBestSample(),1);

    a.Solver.setAndHoldSampleIndex(2);

    fg.solve();

    assertElementsAlmostEqual(a.Solver.getBestSample(),2)
    assertElementsAlmostEqual(b.Solver.getBestSample(),2);


    a.Solver.releaseSampleValue();

    fg.solve();

    assertElementsAlmostEqual(a.Solver.getBestSample(),1);
    assertElementsAlmostEqual(b.Solver.getBestSample(),1);

    a = Real();
    b = Real();
    fg = FactorGraph();
    fg.Solver = 'gibbs';
    fg.Solver.setSeed(0);
    ff = com.analog.lyric.dimple.FactorFunctions.Normal();
    fg.addFactor(ff,a,0.1,b);
    a.Solver.setAndHoldSampleValue(10);
    fg.Solver.setNumSamples(10000);
    fg.Solver.setBurnInUpdates(0);
    fg.Solver.setNumRestarts(0);
    fg.solve();
    assertTrue(abs(b.Solver.getBestSample-10) < .01);
end
