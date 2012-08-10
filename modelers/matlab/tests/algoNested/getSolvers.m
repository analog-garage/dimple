%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function solvers = getSolvers()
    %disp('++algoSumProduct.getSolvers')
    solvers = {};
    solvers = appendCell(solvers, com.analog.lyric.dimple.solvers.sumproduct.Solver());
    %disp('--algoSumProduct.getSolvers')
end
