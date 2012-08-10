%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [ output_args ] = carryless_add( x,y,z )
% This is a delta function that accepts iff
%    x + y = z    (over the real)
% This is *not* mod 2, i.e. it only accepts if there is no carry in the
% sum, assuming x,y,z all have the same range.
if x+y==z
    output_args=1;
else
    output_args=0;
end

end

