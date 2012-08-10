%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [sg,bits,sumToBits] = createSumGraph(N,numBits,numSumToBits)
    %[g,x,y,z,c] = createAdder(numInBits,numOutBits)
    bits = Bit(N,numBits);
    sumToBits = Bit(numSumToBits,1);
    sg = FactorGraph(bits,sumToBits);

    numExtraBits = numSumToBits-numBits;
    extraBits = Bit(numExtraBits,1);
    extraBits.Input = zeros(numExtraBits,1);
    
    prevResultBits = [bits(1,:) extraBits];

    aGraph = createAdder(numSumToBits,numSumToBits);
    
    for i = 2:N
       
       %Get x y z and c
       x = prevResultBits;
       extraBits = Bit(numExtraBits,1);
       extraBits.Input = zeros(numExtraBits,1);
       y = [bits(i,:) extraBits];
       
       if i == N
           z = sumToBits;
       else
           z = Bit(numSumToBits,1);
       end
       
       c = Bit(numSumToBits+1,1);
       c(1).Input = 0;
       c(end).Input = 0;
       
       sg.addFactor(aGraph,x,y,z,c);
       
       prevResultBits = z;
    end
    
    sg.Solver.setNumIterations(100);
end
