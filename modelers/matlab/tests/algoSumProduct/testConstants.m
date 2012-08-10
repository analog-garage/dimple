%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function testConstants()
    %disp(sprintf('\t\t++testConstants'));
    g = FactorGraph;
    v = Variable([4 1 8 9 10]);
    g.addFactor(@constantDelta,v,4);
    g.Solver.setNumIterations(2);
    g.solve;
    assertElementsAlmostEqual(v.Belief,[.5 0 .5 0 0]);    
    %disp(sprintf('\t\t--testConstants'));
end

