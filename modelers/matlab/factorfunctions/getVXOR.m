%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [ xor_out, external_bits ] = getVXOR( n )
%  This function produces a (sub-)factor graph for emulating an m-ary
%  variable using only bits.  (That way, we can use e.g. LLR decoders.)
%  There are n boundary nodes.  The accepting states are where precisely
%  one of the n bits is a 1.  (We can think of it as a "very exclusive
%  XOR", since only one bit is on; thus "VXOR".)

if n==2
    % If n==2, then a VXOR is an XOR
    external_bits=Bit(2);
    xor_out=FactorGraph(external_bits);
    xor_out.addFactor(@xorDelta,external_bits);
    return;
end   

% We want to construct a nearly-balanced binary tree of "carryless_add"
% gates.  We will use an "Ahnentafel list" (cf Wikipedia) in much the same
% way that heaps traditionally do.

% We consider binary trees where each non-leaf node has two children.
% These graphs have 2n-1 nodes (internal and external).

all_bits=Bit(2*n-1,1);
external_bits=all_bits(n:2*n-1);

xor_out=FactorGraph(external_bits);
for i=1:n-1
    xor_out.addFactor(@carryless_add,all_bits(i),all_bits(2*i),all_bits(2*i+1));
end

end

