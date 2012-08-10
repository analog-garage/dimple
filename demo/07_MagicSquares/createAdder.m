%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [g,x,y,z,c] = createAdder(numInBits,numOutBits)
    x = Bit(numInBits,1);
    y = Bit(numInBits,1);
    z = Bit(numOutBits,1);
    c = Bit(numOutBits+1,1);
    g = FactorGraph(x,y,z,c);
    
    numExtraInBits = numOutBits-numInBits;
    if numExtraInBits > 0
        extraXBits = Bit(numExtraInBits,1);
        extraYBits = Bit(numExtraInBits,1);
        x = [x extraXBits];
        y = [y extraYBits];
        setBitInput(extraXBits,zeros(numExtraInBits,1));
        setBitInput(extraYBits,zeros(numExtraInBits,1));
    end
    
    
    for i = 1:numOutBits
       addFactor(g,@adderUnitDelta,x(i),y(i),c(i),c(i+1),z(i)); 
    end
    
    g.Solver.setNumIterations(numOutBits+2);
end
