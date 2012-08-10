%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function testLargeDegree()
    bLog = false;
    dtrace(bLog, '++testLargeDegree');
    tic
    g = FactorGraph();
    b = Variable([1; 0]);
    c = Variable([1; 0]);
    for i = 1:3000
        g.addFactor(@xorDelta,[b c]);
    end
    %Unrelated BUG we found at same time: fg.iterate(1), at least
    % with java solver, fails if NumIterations hasn't been set. 
    % You should probably be able to iterate
    % without NumInterations being set. 

    beliefs = b.Belief;
    assertEqual(beliefs,[.5 .5]);

    g.solve();
    beliefs = b.Belief;
    assertEqual(beliefs,[.5 .5]);
    
    tDiff = toc;
    dtrace(bLog, '--testLargeDegree toc:%u', tDiff);
end
