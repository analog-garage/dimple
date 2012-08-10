%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function testCoreAdjacency()
    %disp('++testCoreAdjacency')
    
    if ~isequal('CSolver',class(getSolver())) && ...
       ~isequal('com.analog.lyric.dimple.solvers.gaussian.Solver',class(getSolver()))
    
        a = Bit();
        b = Bit();
        g = FactorGraph();
        g.addFactor(@xorDelta,a,b);
        result = g.AdjacencyMatrix;
        expected = [0 0 1; 0 0 1; 1 1 0];
        assertEqual(sparse(expected), sparse(result));    
        %disp('--testCoreAdjacency')
    end
end
