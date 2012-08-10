%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function testfunc()
    %disp('++testfunc')
    b = Bit(2,1);
    g = FactorGraph();
    g.addFactor(@myInvFunc,b(1),b(2));
    b.Input = [.8 .3];
    g.Solver.setNumIterations(2);
    g.solve();
    
    x = b(1).Belief;
    y = b(2).Belief;
    
    expected_x = .7*.8/(.7*.8 + .3*.2);
    assertElementsAlmostEqual(expected_x,x(1));    
    assertElementsAlmostEqual(1-expected_x,y(1));    
    %disp('--testfunc')
    
end
