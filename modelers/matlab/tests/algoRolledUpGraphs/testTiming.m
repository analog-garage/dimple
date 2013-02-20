%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2013 Analog Devices, Inc.
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

function testTiming

    N = 1000;
    doUnrolled = true;
    P = .501;

    if doUnrolled
        fg = FactorGraph();
        b = Bit(N,1);

        b.Input = P;
        fg.addFactorVectorized(@xorDelta,b(1:end-1),b(2:end));

        disp('first solve...');
        tic
        fg.solve();
        toc

        disp('second solve...');
        tic
        fg.solve();
        toc

        result1 = b(end).Belief;
    end

    b2 = Bit(2,1);
    ng = FactorGraph(b2);
    ng.addFactor(@xorDelta,b2);

    fg2 = FactorGraph();
    b3 = BitStream();
    fg2.addFactor(ng,b3,b3.getSlice(2));
    b3.DataSource = DoubleArrayDataSource(repmat([1 - P P]',1,N));
    b3.DataSink = DoubleArrayDataSink();
    disp('rolled up graphs...');
    tic
    fg2.solve();
    toc
    result2 = b3.get(2).Belief(2);

    assertElementsAlmostEqual(result1,result2);
end