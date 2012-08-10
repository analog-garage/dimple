%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [ graph,vars ] = getNVarFiniteFieldPlus(poly,n)
    global getNVarFiniteFieldPlusGraphCache___;
    
    if isempty(getNVarFiniteFieldPlusGraphCache___)
        getNVarFiniteFieldPlusGraphCache___ = NVarFiniteFieldPlusCache();
    end
    
    [graph,vars] = getNVarFiniteFieldPlusGraphCache___.get(poly,n);
end

