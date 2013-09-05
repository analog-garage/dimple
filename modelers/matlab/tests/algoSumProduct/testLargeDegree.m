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

function testLargeDegree()
    bLog = false;
    dtrace(bLog, '++testLargeDegree');
    tic
    g = FactorGraph();
    b = Variable([1; 0]);
    c = Variable([1; 0]);
    for i = 1:250
        g.addFactor(@xorDelta,[b c]);
    end
    %Unrelated BUG we found at same time: fg.iterate(1), at least
    % with java solver, fails if NumIterations hasn't been set. 
    % You should probably be able to iterate
    % without NumInterations being set. 

    beliefs = b.Belief;
    assertEqual(beliefs,[.5 .5]');

    g.solve();
    beliefs = b.Belief;
    assertEqual(beliefs,[.5 .5]');
    
    tDiff = toc;
    dtrace(bLog, '--testLargeDegree toc:%u', tDiff);
end
