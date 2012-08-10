%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [ldpc,x] = createLdpc(A, bNested,numIterations)
    if nargin < 1
        A = load('matrixout.txt');
    end
    if nargin < 2
        bNested = true;
    end
    if nargin < 3
        numIterations=100;
    end
    
    blockLength = size(A,2);
    numCheckEquations = size(A,1);
    ldpc = FactorGraph();
    x = Bit(blockLength,1);
    for i = 1:numCheckEquations
        varIndices = find(A(i,:));
        if bNested
            gd = getNBitXorDef(length(varIndices));
            ldpc.addFactor(gd,x(varIndices));
        else
            ldpc.addFactor(@xorDelta,x(varIndices));
        end
    end
    ldpc.Solver.setNumIterations(numIterations);
end
