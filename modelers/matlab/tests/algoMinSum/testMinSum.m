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

function testMinSum()
    bLog = false;
    dtrace(bLog, '++testMinSum');
    
    setSolver('minsum');
    rs=resetRandStream('mt19937ar');

    rows = 8;
    cols = 8;
    numElements = 8;
    domain = 0:numElements-1;
    dtrace(bLog, 'Building graph r:%u c:%u  e:%u', rows, cols, numElements); 
    tic;
    graph = FactorGraph();
    x = Variable(domain,rows,cols);
    for r = 2:rows-1
        for c = 2:cols-1
            graph.addFactor(@MinSumHelper,x(r,c),x(r,c-1),x(r,c+1),x(r-1,c),x(r+1,c));
        end
    end
    tdiff = toc;
    dtrace(bLog, 'Built graph in %u somethings', tdiff);

    p = rand(rs,rows,cols,numElements);
    pn = p ./ repmat(sum(p,3),[1,1,numElements]);
    x.Input = pn;
    graph.Solver.setNumIterations(100);
    dtrace(bLog, 'Solving...');
    tic;
    graph.solve();
    tdiff = toc;
    dtrace(bLog, 'Solved in %u somethings', tdiff);

    load javaMaxProduct8x8.mat;
    diff = sum(sum(sum(abs(beliefs - x.Belief))));
    %assertElementsAlmostEqual(beliefs, x.Belief);    
    %assertElementsAlmostEqual(diff, 0, );
    epsilon = 1e-12;
    if strcmp(class(getSolver()),'com.analog.lyric.dimple.solvers.minsumfixedpoint.Solver') == 1
        % The fixed point solver now uses an unsigned scale by default.
        % Because of this, it is restricted to a maximum width of 31 bits.
        % This restriction is enough to cause the solver to require a
        % larger epsilon.
        epsilon = 1e-5;
    end
    assertTrue(diff < epsilon);

    dtrace(bLog, '--testMinSum');
end
