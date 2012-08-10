%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [xordef,b] = getNBitXorDef(n)

    global getNVBitXorDefGraphCache___;
    
    if isempty(getNVBitXorDefGraphCache___)
        getNVBitXorDefGraphCache___ = NBitXorCache();
    end

    [xordef,b] = getNVBitXorDefGraphCache___.get(n);
end
