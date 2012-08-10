%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function testNDimensions()
    %disp('++testNDimensions')
    b = Bit(2,2,3);
    p  = ones(2,2,3);
    b.Input = p;
    beliefs = b.Belief;
    assertElementsAlmostEqual(beliefs,p);
    
    btmp = b(1,1,:);
    ptmp = p(1,1,:);
    beliefs = btmp.Belief;
    assertElementsAlmostEqual(ptmp,beliefs);
    %disp('--testNDimensions')
    
end
