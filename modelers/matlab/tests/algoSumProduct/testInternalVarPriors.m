%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function testInternalVarInput()
    %disp('++testInternalVarInput')
    b = Bit();
    g = FactorGraph();
    c = Bit();
    c.Input = .8;
    g.addFactor(@xorDelta,[b; c]);

    g.solve();
    beliefs = b.Belief;
    assertElementsAlmostEqual(.8,beliefs(1));
    %disp('--testInternalVarInput')
end
